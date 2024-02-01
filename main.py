from fastapi import FastAPI, Request
from typing import List
from pydantic import BaseModel
import joblib
import numpy as np


app = FastAPI()

class Item(BaseModel):
    Station: List[str]
    Skill: str


# Load the model
best_energy_model_station1 = joblib.load('Dataset_Lab2_22_23/Dataset_Lab2/best_energy_model_dataset_station1.csv_BayesianRidge.joblib')
best_energy_model_station2 = joblib.load('Dataset_Lab2_22_23/Dataset_Lab2/best_energy_model_dataset_station2.csv_SGDRegressor.joblib')
best_energy_model_station3 = joblib.load('Dataset_Lab2_22_23/Dataset_Lab2/best_energy_model_dataset_station3.csv_Ridge.joblib')
best_energy_model_station4 = joblib.load('Dataset_Lab2_22_23/Dataset_Lab2/best_energy_model_dataset_station4.csv_LinearRegression.joblib')

best_velocity_model_station1 = joblib.load('Dataset_Lab2_22_23/Dataset_Lab2/best_velocity_model_dataset_station1.csv_Lasso.joblib')
best_velocity_model_station2 = joblib.load('Dataset_Lab2_22_23/Dataset_Lab2/best_velocity_model_dataset_station2.csv_Lasso.joblib')
best_velocity_model_station3 = joblib.load('Dataset_Lab2_22_23/Dataset_Lab2/best_velocity_model_dataset_station3.csv_Lasso.joblib')
best_velocity_model_station4 = joblib.load('Dataset_Lab2_22_23/Dataset_Lab2/best_velocity_model_dataset_station4.csv_SGDRegressor.joblib')





def get_velocity_energy_predic(station: str,skill: str):

        # Mapping of skill strings to numbers
    skill_mapping = {
        "sk_g_a": 1,
        "sk_g_b": 2,
        "sk_g_c": 3
    }
    
    # Convert the skill string to a number based on the mapping
    print('entrou aqui')
    skill_number = skill_mapping.get(skill, 0)  # Use 0 as default if skill is not found

    # Convert the value into a NumPy array with the correct shape
    input_data = np.array([[skill_number]])
    #input_data = np.array([skill_number]).reshape(1,-1)
    #input_data = np.array(skill_number).reshape(1,1)
    #input_data = np.array(skill_number).reshape(1, -1)

    if station == "GlueStation1":
        prediction_Energy_station1 = float(best_energy_model_station1.predict(input_data))
        prediction_Velocity_station1 = float(best_velocity_model_station1.predict(input_data))
        return {'Energy':prediction_Energy_station1,'Velocity':prediction_Velocity_station1}
    
    elif station == "GlueStation2":
        prediction_Energy_station2 = float(best_energy_model_station2.predict(input_data))
        prediction_Velocity_station2= float(best_velocity_model_station2.predict(input_data))
        return {'Energy':prediction_Energy_station2,'Velocity':prediction_Velocity_station2}
    
    elif station == "GlueStation3":
        prediction_Energy_station3 = float(best_energy_model_station3.predict(input_data))
        prediction_Velocity_station3 =float(best_velocity_model_station3.predict(input_data))

        return {'Energy':prediction_Energy_station3,'Velocity':prediction_Velocity_station3}
    
    elif station == "GlueStation4":
        prediction_Energy_station4 = float(best_energy_model_station4.predict(input_data))
        prediction_Velocity_station4=float(best_velocity_model_station4.predict(input_data))

        return {'Energy':prediction_Energy_station4,'Velocity':prediction_Velocity_station4}
    else:
        return {'Energy':0,'Velocity':0}




def get_prediction(station: str,skill: str):
    dicEnergy_Velocity = get_velocity_energy_predic(station,skill)
    print(dicEnergy_Velocity)
    return dicEnergy_Velocity

@app.post("/prediction")
def get_items(item: Item):
    stations = item.Station
    skill = item.Skill
    print(f"Received data: {stations} and {skill}")

    predictions = {station : get_prediction(station,item.Skill) for station in stations}
    print(predictions)
    return {
    "status" : "SUCCESS",
    "predictions" :predictions

}

#print(f"Received data: {stations} and {skill}")

# async def getInformation(info : Request):
#     req_info = await info.json()
#     return {
#         "status" : "SUCCESS",
#         "data" : req_info
#     }
