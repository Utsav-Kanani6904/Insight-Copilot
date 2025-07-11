from langchain.prompts import PromptTemplate, ChatPromptTemplate
from langchain_core.messages import HumanMessage, AIMessage
from dataclasses import replace
import pandas as pd
import psycopg2

from ..models.states import InputState, AgentState
from ..models.schemas import QueryRewritePlan, TableColumnPlan, SQLQueryResponse, CorrectedQuery, ChartSuggestion
from .utils import extract_sql_block
from ..core.config import settings
from langchain_google_genai import ChatGoogleGenerativeAI
from pinecone import Pinecone

llm = ChatGoogleGenerativeAI(model="gemini-1.5-flash",google_api_key=settings.GOOGLE_API_KEY)

pc = Pinecone(api_key=settings.PINECONE_API_KEY)
index = pc.Index(host=settings.PINECONE_HOST)
table_namespace = "table-details-university"      # In Production namespace : settings.USERNAME + "-" + settings.DATABASE_CONFIG_ID + "-tabledetails"
column_namespace = "column-details-university"    # In Production namespace : settings.USERNAME + "-" + settings.DATABASE_CONFIG_ID + "-columnDetails"
query_namespace = "query-example-university"      # In Production namespace : settings.USERNAME + "-" + settings.DATABASE_CONFIG_ID + "-queryExample"

print("Username is : ", settings.USERNAME)
print("Database Config ID is : ", settings.DATABASE_CONFIG_ID)


###############################################################

def rewrite_question(state : InputState)-> AgentState:
    """Rewrites the user's question to make it more explicit and detailed for SQL planning."""
    question = state.messages[-1].content if state.messages else ""
    tables = state.tables if state.tables else []
    print("Entering rewrite question.....")
    
    prompt = PromptTemplate.from_template("""
    You are a SQL planning assistant.

    Given a user's natural language question:
    1. Rewrite it in a clear, explicit and detailed form that makes all the necessary table and column relationships obvious.
    2. Make sure the rewritten version **asks to display all useful columns**
    3. If the original question is vague (e.g., “top students”), clarify the logic (e.g., “students with the highest average grade”).
    4. Use terminology similar to column or table names when possible (e.g., "student name" instead of "who" or "person").
    5. Identify which tables are required.
    6. For each table, list the descriptions of columns you expect to use and why.
    7. A form that can help a retriever or another AI system better identify relevant tables or columns.
                
    Return your result in the required structured format.
                                        
    ## This are the tables : {table}


    Original Question: {question}
    """)

    
    question_rewrite_chain = prompt | llm.with_structured_output(QueryRewritePlan)
    result = question_rewrite_chain.invoke({
        "question": question,
        "table": tables
    })

    print("Rewritten Question: ", result.rewritten_question)
    print("Updated Question: ", result.updated_question)

    return AgentState(
            remaining_datafetch=1,  # This is the default value setted for testing can change later
            remaining_querygen=3,   # This is the default value setted for testing can change later
            question=question,
            rewritten_question=result.rewritten_question,
            updated_question=result.updated_question,
            relevant_queries=[],
            relevant_tables=[],
            relevant_columns=[],
            already_seen_chunk_column=set(),
            already_seen_chunk_table=set(),
            error_message="",
            explanation="",
            is_sufficient_data=False,
            sql_query="",
            query_executed_successfully=False,
            result=[],
        )

###############################################################

def get_relevant_queries(state : AgentState)->AgentState:
    # print(state)
    print("Entering relevant_queries.....")
    
    query_fetch = index.search(
        namespace=query_namespace, 
        query={
            "inputs": {"text":  state.rewritten_question}, 
            "top_k": 7
        },
    )

    relevant_queries = []
    for docs in query_fetch.result.hits:
        relevant_queries.append(docs.fields.get("chunk_text", ""))
    
    # print(relevant_queries)

    state.relevant_queries = relevant_queries

    return state

###############################################################

def get_table_and_columns(state : AgentState)->AgentState:
    print("Entering get relevant column.....")
    
    table_fetch = index.search(
        namespace=table_namespace, 
        query={
            "inputs": {"text":  state.rewritten_question}, 
            "top_k": 5
        },
    )
    # print(results.result.hits)

    column_fetch = index.search(
        namespace=column_namespace, 
        query={
            "inputs": {"text":  state.rewritten_question}, 
            "top_k": 8
        },
    )

    relevant_tables = []
    relevant_columns = []


    for docs in table_fetch.result.hits:
        chunkid = docs._id

        if chunkid not in state.already_seen_chunk_table:
            relevant_tables.append(docs.fields)
            state.already_seen_chunk_table.add(chunkid)

    for docs in column_fetch.result.hits:
        chunkid = docs._id

        if chunkid not in state.already_seen_chunk_column:
            relevant_columns.append(docs.fields.get("chunk_text", ""))
            state.already_seen_chunk_column.add(chunkid)

    # print(relevant_tables)
    # print(relevant_columns)
    state.relevant_tables = relevant_tables
    state.relevant_columns = relevant_columns
    
    return state

###############################################################

def generate_sql_query(state : AgentState)->AgentState:

    print("Entering generate_sql_query.....")
    

    prompt = ChatPromptTemplate.from_messages([
    ("system", """You are a helpful AI that generates SQL queries and explains them."""),
    ("human",
    """
    You are a helpful AI assistant designed to generate SQL queries and explain them clearly.

    Your task is to generate a **valid SQL query** and a **step-by-step explanation** based on:
    1. A natural language question from the user.
    2. A list of relevant tables.
    3. A list of relevant columns with metadata (including descriptions, types, and foreign key relationships).
    4. A few-shot list of example SQL queries with their corresponding questions (for context; they may or may not be directly related).

    ---

    **Guidelines:**
    - DON'T ASSUME ANYTHING BY YOURSELF!!!! 
    - Use **only** the tables and columns provided.
    - When filtering by a string (e.g., using `WHERE`), **always wrap the string in single quotes** `'like this'` — never use double quotes `"like this"`.
    - Example: `WHERE department = 'Physics'` ✅  
               `WHERE department = "Physics"` ❌

    - Use appropriate **JOINs** by identifying how foreign keys connect one table to another. Multiple joins across related tables are allowed and encouraged if needed to fulfill the query.
    - Use **aggregation** (such as `COUNT`, `AVG`, `SUM`, etc.) when it is required to summarize data as part of answering the question.
    - Always analyze if multiple tables need to be joined to access necessary information (e.g., join User → Certificate → Institute to find users with certificates from a specific institute).
    - Columns come with semantic descriptions; **infer intent** from those descriptions even if exact wording doesn't match the question.
    

    ### Natural Language Question:
    {user_question}

    ### Relevant Tables:
    {relevant_tables}

    ### Relevant Columns:
    {relevant_columns}

    ### Few-shot Examples:
    {few_shot_examples}

    ---

    Return your answer in this structured format:
    - `sql_query`: the final SQL query string (wrapped in backticks, like `SELECT * ...`) (or `"N/A"` if insufficient data)
    - `explanation`: detailed explanation of how the query was constructed or what information is missing
    - `is_sufficient_data`: Boolean indicating whether the provided data is sufficient to construct a valid SQL query
    """)
    ])

    sql_answer_chain = prompt | llm.with_structured_output(SQLQueryResponse)

    response = sql_answer_chain.invoke({
        "user_question": state.updated_question,
        "relevant_tables": state.relevant_tables,
        "relevant_columns": state.relevant_columns,
        "few_shot_examples": state.relevant_queries,
    })

    raw_sql = extract_sql_block(response.sql_query)

    state.sql_query = raw_sql
    state.explanation = response.explanation
    state.is_sufficient_data = response.is_sufficient_data

    return state

###############################################################

def get_more_table_column(state : AgentState)->AgentState:
    print("Entering get more table column.....")
    
    table_fetch = index.search(
        namespace=table_namespace, 
        query={
            "inputs": {"text":  state.explanation}, 
            "top_k": 3
        },
    )
    # print(results.result.hits)

    column_fetch = index.search(
        namespace=column_namespace, 
        query={
            "inputs": {"text":  state.explanation}, 
            "top_k": 5
        },
    )

    relevant_tables = []
    relevant_columns = []


    for docs in table_fetch.result.hits:
        chunkid = docs._id

        if chunkid not in state.already_seen_chunk_table:
            relevant_tables.append(docs.fields)
            state.already_seen_chunk_table.add(chunkid)

    for docs in column_fetch.result.hits:
        chunkid = docs._id

        if chunkid not in state.already_seen_chunk_column:
            relevant_columns.append(docs.fields.get("chunk_text", ""))
            state.already_seen_chunk_column.add(chunkid)

    # print(relevant_tables)
    # print(relevant_columns)
    state.remaining_datafetch -= 1
    state.relevant_tables = relevant_tables
    state.relevant_columns = relevant_columns
    
    return state

###############################################################

from dataclasses import replace
import psycopg2
def execute_query(state : AgentState)->AgentState:

    print("Entering executre_query.....")
    
    database =  settings.DATABASE_NAME
    sql_query = state.sql_query
    result = []
    error_message = ""
    print("SQL Query is : ",sql_query)
    try:
        # Establish connection
        conn = psycopg2.connect(
            host=settings.DATABASE_HOST,
            port=settings.DATABASE_PORT,
            database=database,
            user=settings.DATABASE_USER,
            password=settings.DATABASE_PASSWORD
        )

        cur = conn.cursor()
        query = sql_query
        cur.execute(query)
        
        column_names = [desc[0] for desc in cur.description]
        
        rows = cur.fetchall()

        result = [dict(zip(column_names, row)) for row in rows]
        state.query_executed_successfully = True

        cur.close()
        conn.close()

    except Exception as e:
        error_message = e
    
    error_str = str(error_message)

    state.result = result
    state.error_message = error_str
    print("Result is : ", result)
    print("Error is : ", error_str)

    return state


###############################################################

def regenerate_query(state : AgentState)->AgentState:
    print("Entering regenerate_query.....")
    

    prompt = ChatPromptTemplate.from_messages([
    ("system", """You are a helpful AI that generates SQL queries and explains them."""),
    ("human",
    """
        You are a highly skilled SQL assistant helping to correct SQL queries.

        You are given:
        1. An **original user question**
        2. A previously generated SQL query that caused an execution error.
        3. The **error message** returned by the database.
        4. A brief **explanation of the query logic** that was originally followed.

        Your task is to:
        - Analyze the error message and understand what went wrong.
        - Use the explanation of the original query to understand the user's intent.
        - Fix the SQL query accordingly.
        - Ensure that the new query uses correct table and column names, and valid SQL syntax.
        - Do **not** repeat the same structural mistake.
        - If the query failed due to incorrect table or column names (that don't exist), DO NOT guess. Instead:
            - Set `corrected_sql` to "N/A"
            - In `reason_for_fix`, explain which table or column was invalid and what kind of table/column is needed.
                (e.g., "The column `student_score` does not exist. A numeric column representing student performance is needed.")


        ### Original User Question:
        {question}

        ### Original SQL Query:
        {original_sql}

        ### Query Explanation:
        {explanation}

        ### Error Message:
        {error}

    """)
    ])

    query_regenerate_chain = prompt | llm.with_structured_output(CorrectedQuery)

    response = query_regenerate_chain.invoke({
        "question": state.updated_question,
        "original_sql": state.sql_query,
        "explanation": state.explanation,
        "error": state.error_message,
    })

    state.remaining_querygen -= 1

    if(response.corrected_sql == "N/A"):
        state.explanation = response.reason_for_fix

    state.sql_query = response.corrected_sql 
    return state


###############################################################



def generate_chart_insight(state: AgentState) -> AgentState:
    print("Entering generate_chart_insight.....")
    print(len(state.result))
    if len(state.result) == 0:
        return replace(state, insight="No data to visualize.")

    df = pd.DataFrame(state.result) 
    question = state.question if hasattr(state, "question") else "Unknown"

    schema = [
        {"column_name": col, "dtype": str(dtype)}
        for col, dtype in zip(df.columns, df.dtypes)
    ]
    sample = df.head(3).to_dict(orient="records")

    print(schema)
    print(sample)

    prompt = ChatPromptTemplate.from_messages([
    ("system", """You are a helpful data visualization assistant."""),
    ("human",
    """
        You are a helpful data visualization assistant.
        Your job is to decide whether a chart should be generated based on the user's question, column schema, and sample data.

        ## Your Responsibilities:
        1. Analyze the user's intent from the natural language question.
        2. Determine if a chart will be helpful to visualize the answer.
        3. If yes, choose a suitable chart type from this list:
        - "bar", "line", "pie", "histogram", "scatter"
        4. Select one column for the X-axis and one for the Y-axis (if applicable).
        - Not all columns must be used.
        5. If no chart is suitable, clearly explain why (e.g., text-based result, too few rows, not numeric data, etc.)

        ### Input:
        Question: {question}

        Schema:
        {schema}

        Sample Rows:
        {sample}

        ### Output format:
        - chart_type: one of the allowed types, or leave empty if no chart is suitable
        - x_axis: column name for X-axis (if applicable)
        - y_axis: column name for Y-axis (if applicable)
        - reason: why this chart is appropriate OR why it is not possible
    """)
    ])

    llm_chart_chain = prompt | llm.with_structured_output(ChartSuggestion)
    
    suggestion =  llm_chart_chain.invoke({
        "question": question,
        "schema": schema,
        "sample": sample
    })

    return replace(
        state,
        chart_type=suggestion.chart_type,
        x_axis=suggestion.x_axis,
        y_axis=suggestion.y_axis,
        insight=suggestion.reason
    )
