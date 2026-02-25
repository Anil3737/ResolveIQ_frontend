import sys, os
print("1. Adding path...")
sys.path.insert(0, r"c:\Users\DELL\OneDrive\Desktop\resolveiq_backend")

print("2. Importing RiskEngine...")
# Using a more direct import to avoid app.__init__ if possible
from app.ai.risk_engine import RiskEngine

print("3. Starting tests...")
def test_engine():
    result = RiskEngine.calculate("Test title", "Test description")
    print(f"Result: {result['score']}")

if __name__ == "__main__":
    test_engine()
    print("4. Done.")
