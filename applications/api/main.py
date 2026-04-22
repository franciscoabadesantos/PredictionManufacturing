from pathlib import Path
from typing import Dict, List

import joblib
import numpy as np
from fastapi import FastAPI
from pydantic import BaseModel


app = FastAPI(title="Manufacturing Prediction API")

ROOT_DIR = Path(__file__).resolve().parents[2]
MODELS_DIR = ROOT_DIR / "data" / "models"

STATION_MODELS = {
    "GlueStation1": {
        "energy": MODELS_DIR / "best_energy_model_dataset_station1.csv_BayesianRidge.joblib",
        "velocity": MODELS_DIR / "best_velocity_model_dataset_station1.csv_Lasso.joblib",
    },
    "GlueStation2": {
        "energy": MODELS_DIR / "best_energy_model_dataset_station2.csv_SGDRegressor.joblib",
        "velocity": MODELS_DIR / "best_velocity_model_dataset_station2.csv_Lasso.joblib",
    },
    "GlueStation3": {
        "energy": MODELS_DIR / "best_energy_model_dataset_station3.csv_Ridge.joblib",
        "velocity": MODELS_DIR / "best_velocity_model_dataset_station3.csv_Lasso.joblib",
    },
    "GlueStation4": {
        "energy": MODELS_DIR / "best_energy_model_dataset_station4.csv_LinearRegression.joblib",
        "velocity": MODELS_DIR / "best_velocity_model_dataset_station4.csv_SGDRegressor.joblib",
    },
}

SKILL_MAPPING = {
    "sk_g_a": 1,
    "sk_g_b": 2,
    "sk_g_c": 3,
}

LOADED_MODELS = {
    station: {
        "energy": joblib.load(paths["energy"]),
        "velocity": joblib.load(paths["velocity"]),
    }
    for station, paths in STATION_MODELS.items()
}


class Item(BaseModel):
    Station: List[str]
    Skill: str


def get_prediction(station: str, skill: str) -> Dict[str, float]:
    model_group = LOADED_MODELS.get(station)
    skill_number = SKILL_MAPPING.get(skill, 0)

    if model_group is None:
        return {"Energy": 0.0, "Velocity": 0.0}

    input_data = np.array([[skill_number]])
    energy_prediction = float(model_group["energy"].predict(input_data)[0])
    velocity_prediction = float(model_group["velocity"].predict(input_data)[0])

    return {
        "Energy": energy_prediction,
        "Velocity": velocity_prediction,
    }


@app.get("/")
def healthcheck() -> Dict[str, str]:
    return {"status": "ok", "service": "manufacturing-prediction-api"}


@app.post("/prediction")
def predict(item: Item) -> Dict[str, object]:
    predictions = {
        station: get_prediction(station, item.Skill)
        for station in item.Station
    }

    return {
        "status": "SUCCESS",
        "predictions": predictions,
    }
