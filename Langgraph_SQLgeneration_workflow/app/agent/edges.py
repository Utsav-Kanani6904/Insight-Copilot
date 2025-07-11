from typing import Literal
from ..models.states import AgentState
from langgraph.graph import END

def is_query_generated(state : AgentState)->Literal["get_more_table_column", "execute_query", END]:
    if(state.remaining_datafetch > 0 and state.is_sufficient_data == False):
        return "get_more_table_column"
    elif(state.is_sufficient_data == True):
        return "execute_query"
    else:
        return END

def is_sqlquery_right(state : AgentState)->Literal["regenerate_query", "generate_chart_insight", END]:
    if(state.query_executed_successfully == True):
        print("Query executed successfully, generating chart insight...")
        print("------------------------------------")
        return "generate_chart_insight"
    elif(state.remaining_querygen > 0):
        return "regenerate_query"
    else:
        return END


def can_fix_sqlerror(state : AgentState)->Literal["get_more_table_column", "execute_query"]:
    if(state.sql_query == "N/A"):
        return "get_more_table_column"
    else:
        return "execute_query"
    