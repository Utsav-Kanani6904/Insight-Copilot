from langchain_core.messages import AnyMessage
from langgraph.graph import add_messages
from typing import Annotated, Dict, List, Optional, Sequence, Set
from dataclasses import dataclass, field

@dataclass
class InputState:
    """Defines the input state for the agent, representing a narrower interface to the outside world.

    This class is used to define the initial state and structure of incoming data.
    """
    messages : Annotated[Sequence[AnyMessage], add_messages] = field(default_factory=list)
    tables : List[str] = field(default_factory=list)

@dataclass
class AgentState:
    remaining_datafetch: int = 1
    remaining_querygen: int = 3
    question : str = ""
    rewritten_question : str = ""
    updated_question : str = ""
    relevant_queries : List[str] = field(default_factory=list)
    relevant_tables : List[str] = field(default_factory=list)
    relevant_columns : List[str] = field(default_factory=list)
    already_seen_chunk_column: Set[str] = field(default_factory=set)
    already_seen_chunk_table: Set[str] = field(default_factory=set)
    error_message : str = ""
    explanation : str = ""
    is_sufficient_data : bool = False
    sql_query : str = ""
    query_executed_successfully : bool = False
    result : List[str] =  field(default_factory=list)
    chart_type: Optional[str] = None
    x_axis: Optional[str] = None
    y_axis: Optional[str] = None
    insight: Optional[str] = None