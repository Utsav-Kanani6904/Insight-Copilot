from fastapi import APIRouter, HTTPException
from langchain_core.messages import HumanMessage
import logging

from ..models.states import InputState
from ..models.schemas import SQLQueryRequest, SQLQueryResult
from ..agent.workflow import graph
from ..core.config import settings

logger = logging.getLogger(__name__)
router = APIRouter()

@router.post("/sql-query", response_model=SQLQueryResult)
async def generate_sql_query(request: SQLQueryRequest):
    try:
        # Create input state
        input_state = InputState(
            messages=[HumanMessage(content=request.message)],
            tables=request.tables
        )

        print("Input State: ", request.tables)
        print("---------------------------------")

        settings.DATABASE_HOST = request.database.host
        settings.DATABASE_PORT = request.database.port
        settings.DATABASE_USER = request.database.user
        settings.DATABASE_PASSWORD = request.database.password
        settings.DATABASE_NAME = request.database.dbName
        settings.DATABASE_CONFIG_ID = request.database.id
        settings.USERNAME = request.database.username
        
        # Invoke the agent
        result = await graph.ainvoke(input_state)
        # print("Graph ran successfully ", result)

        # print("Sql_query : ", result["sql_query"])
        # print("explanation : ", result["explanation"])
        # print("result : ",  result["result"])
        # print("query_executed_successfully : ", result["query_executed_successfully"], type(result["query_executed_successfully"]))

        sqlQueryResult = SQLQueryResult(
            sql_query=result["sql_query"],
            explanation=result["explanation"],
            result=result["result"],
            query_executed_successfully="True" if result["query_executed_successfully"] else "False",
            status="success" if result["query_executed_successfully"] else "completed",
            chart_type = result["chart_type"],
            x_axis = result["x_axis"],
            y_axis = result["y_axis"],
            insight = result["insight"]
        )

        print(sqlQueryResult.json())

        return sqlQueryResult
        
    except Exception as e:
        logger.error(f"Error generating SQL: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))

@router.get("/health")
async def health_check():
    return {"status": "healthy", "service": "SQL Agent"}
