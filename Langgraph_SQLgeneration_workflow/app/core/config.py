import os
from dotenv import load_dotenv

load_dotenv()

class Settings:
    GOOGLE_API_KEY = os.getenv("GOOGLE_API_KEY")
    DATABASE_HOST = os.getenv("DATABASE_HOST")
    DATABASE_PORT = os.getenv("DATABASE_PORT")
    DATABASE_USER = os.getenv("DATABASE_USER")
    DATABASE_PASSWORD = os.getenv("DATABASE_PASSWORD")
    DATABASE_NAME = os.getenv("DATABASE_NAME")
    PINECONE_API_KEY = os.getenv("PINECONE_API_KEY")
    PINECONE_HOST = os.getenv("PINECONE_HOST")
    API_HOST: str = os.getenv("API_HOST", "0.0.0.0")
    API_PORT: int = int(os.getenv("API_PORT", 8000))
    API_RELOAD = os.getenv("API_RELOAD")
    DATABASE_CONFIG_ID = os.getenv("DATABASE_CONFIG_ID", "temp")
    USERNAME = os.getenv("USERNAME", "temp")

settings = Settings()
