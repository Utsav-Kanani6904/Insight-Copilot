import re

def extract_sql_block(text: str) -> str:
    match = re.search(r"```sql\s+(.*?)```", text, re.DOTALL)
    if match:
        return match.group(1).strip()
    return ""

