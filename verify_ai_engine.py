import sys, os
import json

# Add backend to path
sys.path.insert(0, r"c:\Users\DELL\OneDrive\Desktop\resolveiq_backend")

# Import the class directly
from app.ai.risk_engine import RiskEngine

def test_engine():
    test_cases = [
        {
            "title": "Server Down - Emergency",
            "description": "The main database server is down. Entire building is unable to work. Urgent ASAP!",
            "expected_high": True
        },
        {
            "title": "Need help with printer",
            "description": "My printer is slow and won't print double-sided.",
            "expected_high": False
        },
        {
            "title": "Security Breach - Unauthorized access detected",
            "description": "We detected unauthorized access to several user accounts. Production system might be compromised.",
            "expected_high": True
        },
        {
            "title": "Email not working for all users",
            "description": "Nobody in the office can send emails right now. Production is halted.",
            "expected_high": True
        }
    ]

    print("--- AI RISK ENGINE VERIFICATION (ISOLATED) ---")
    for i, case in enumerate(test_cases):
        result = RiskEngine.calculate(case['title'], case['description'])
        print(f"\nTest Case {i+1}: {case['title']}")
        print(f"Score: {result['score']}")
        print(f"Summary: {result['summary']}")
        print(f"Explanation: {json.dumps(result['explanation'], indent=2)}")
        
        # Simple threshold for verification
        if case['expected_high'] and result['score'] < 40:
            print("❌ FAIL: Expected high score but got low.")
        elif not case['expected_high'] and result['score'] > 40:
            print("❌ FAIL: Expected low score but got high.")
        else:
            print("✅ PASS")

if __name__ == "__main__":
    test_engine()
    print("\nVerification Complete.")
