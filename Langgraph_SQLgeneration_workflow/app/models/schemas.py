from pydantic import BaseModel, Field
from typing import List, Literal, Optional, Dict, Any
from langchain_core.messages import AnyMessage


class TableColumnPlan(BaseModel):
    table_name: str = Field(
        ...,
        description="The name of the table that will be used in the SQL query."
    )
    expected_columns: List[str] = Field(
        ...,
        description=(
            "A list of descriptions of the types of data expected from this table. "
            "Do not use exact column names. Instead, describe what kind of information is needed. "
            "Example: 'user ID of the employee', 'Name of the employee', 'name of the skill', 'company identifier', 'Price of the item',etc. This type of short description"
        )
    )
    purpose: str = Field(
        ...,
        description="A short explanation of why this table is needed in the context of answering the query."
    )



class QueryRewritePlan(BaseModel):
    original_question: str = Field(
        ...,
        description="The user's original natural language question."
    )
    rewritten_question: str = Field(
        ...,
        description=(
            "A rewritten version of the original question that clearly explains the intent, "
            "mentions relevant entities, and makes relationships between tables obvious. "
            "This should not be a SQL query, but a detailed version of the natural language question."
        )
    )
    updated_question: str = Field(
        ...,
        description=(
            "An enhanced version of the original question that explicitly mentions what the user "
            "expects to see in the result — including useful fields like names, IDs, totals, etc. "
            "If you think it required filtering or sorting then also mention that data on which you want to filter or sort. "
            "Keep it in plain natural language (no SQL terms), and do not mention specific table or column names."
        )
    )


class SQLQueryResponse(BaseModel):
    """
    Structured response for SQL query generation.
    """
    sql_query: str = Field(
        ...,
        description="""The generated SQL query string. the SQL query string wrapped in backticks like:
        ```sql
        SELECT * FROM ... WHERE department = 'Science';
        ```
        If insufficient data, write N/A instead of query."""
    )
    explanation: str = Field(
        ...,
        description="Step-by-step explanation of how the query was created, or if insufficient data, explain what is missing."
    )
    is_sufficient_data: bool = Field(
        ...,
        description="True if all required tables and columns were provided to generate a valid query, else False."
    )



class CorrectedQuery(BaseModel):
    corrected_sql: str = Field(
        ...,
        description=(
            "The revised SQL query that resolves the error and correctly answers the original question. "
            "If the error is due to incorrect or unknown table/column names, set this to 'N/A'."
        )
    )
    reason_for_fix: Optional[str] = Field(
        None,
        description=(
            "A clear explanation of what was wrong with the original query. "
            "If the fix was not possible due to invalid names (e.g., missing table or column), "
            "list which table/column names were invalid and describe what kind of table/column is needed. "
            "This will be used to re-fetch relevant metadata."
        )
    )


class ChartSuggestion(BaseModel):
    chart_type: Optional[Literal["bar", "line", "pie", "histogram", "scatter"]] = Field(
        default=None,
        description="Chart type to visualize the data. Leave empty if no chart is appropriate."
    )
    x_axis: Optional[str] = Field(
        default=None,
        description="Column to be used for X-axis (if applicable). If not applicable, Leave empty."
    )
    y_axis: Optional[str] = Field(
        default=None,
        description="Column to be used for Y-axis (if applicable). If not applicable, Leave empty."
    )
    reason: str = Field(
        description="Short explanation of why this chart is appropriate OR why no chart can be created"
    )


class DatabaseConfig(BaseModel):
    id: Optional[str] = None
    userid: Optional[str] = None
    description: Optional[str] = None
    username : Optional[str] = None
    dbName: str
    host: str
    port: int
    user: str
    password: str

class SQLQueryRequest(BaseModel):
    message: str
    tables: List[str]
    user_id: Optional[str] = None
    database: DatabaseConfig  


class SQLQueryResult(BaseModel):
    sql_query: str
    explanation: str
    result: List[Dict[str, Any]]
    query_executed_successfully: str
    status: str
    chart_type: Optional[str] = None
    x_axis: Optional[str] = None
    y_axis: Optional[str] = None
    insight: Optional[str] = None