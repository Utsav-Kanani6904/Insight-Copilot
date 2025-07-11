from langgraph.graph import StateGraph, START, END
from .nodes import (
    rewrite_question, get_relevant_queries, get_table_and_columns,
    generate_sql_query, get_more_table_column, execute_query, regenerate_query, generate_chart_insight
)
from .edges import is_query_generated, is_sqlquery_right, can_fix_sqlerror
from ..models.states import InputState, AgentState

def create_workflow():
    workflow = StateGraph(AgentState, input=InputState)

    #Add Nodes
    workflow.add_node("rewrite_question", rewrite_question)
    workflow.add_node("get_relevant_queries", get_relevant_queries)
    workflow.add_node("get_relevant_table_column", get_table_and_columns)
    workflow.add_node("generate_sql_query", generate_sql_query)
    workflow.add_node("get_more_table_column", get_more_table_column)
    workflow.add_node("execute_query", execute_query)
    workflow.add_node("regenerate_query",regenerate_query)
    workflow.add_node("generate_chart_insight", generate_chart_insight)

    # Add Edges
    workflow.add_edge(START,"rewrite_question")
    workflow.add_edge("rewrite_question","get_relevant_queries")
    workflow.add_edge("get_relevant_queries", "get_relevant_table_column")
    workflow.add_edge("get_relevant_table_column", "generate_sql_query")
    workflow.add_conditional_edges("generate_sql_query", is_query_generated)
    workflow.add_edge("get_more_table_column", "generate_sql_query")
    workflow.add_conditional_edges("execute_query",is_sqlquery_right)
    workflow.add_conditional_edges("regenerate_query",can_fix_sqlerror)
    workflow.add_edge("generate_chart_insight", END)
    
    return workflow.compile()

graph = create_workflow()


