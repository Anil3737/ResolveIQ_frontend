import pymysql
import os
from dotenv import load_dotenv

load_dotenv()

DB_CONFIG = {
    'host': os.getenv('DB_HOST', '127.0.0.1'),
    'user': os.getenv('DB_USER', 'root'),
    'password': os.getenv('DB_PASSWORD', ''),
    'database': os.getenv('DB_NAME', 'resolveiq'),
    'cursorclass': pymysql.cursors.DictCursor
}

def check_depts():
    try:
        connection = pymysql.connect(**DB_CONFIG)
        with connection.cursor() as cursor:
            print("\n--- DEPARTMENTS ---")
            cursor.execute("SELECT id, name FROM departments")
            for row in cursor.fetchall():
                print(f"ID: {row['id']} | Name: {row['name']}")
                
    except Exception as e:
        print(f"Error: {e}")
    finally:
        if 'connection' in locals():
            connection.close()

if __name__ == "__main__":
    check_depts()
