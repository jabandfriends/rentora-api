import sys
import json
import pandas as pd
import joblib

model = joblib.load("maintenance_model.pkl")

input_json = sys.argv[1]
data = json.loads(input_json)

df = pd.DataFrame([data])

prediction = model.predict(df)[0]

print(json.dumps({"predicted_days": int(prediction)}))