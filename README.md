# PredictionManufacturing

Machine learning for energy consumption prediction in a manufacturing system, combined with a JADE-based control system for coordinating production resources.

## Project Overview

The system is composed of four glue application stations and one AGV responsible for transporting products between stations. Each station supports a different set of glue skills, and the control system must decide where each product should go based on its required process plan and resource availability.

### Skill Distribution Per Station

| Resource / Station | Location ID | Skill ID |
| --- | --- | --- |
| Glue Station #1 | `GlueStation1` | Glue Type A, Glue Type B |
| Glue Station #2 | `GlueStation2` | Glue Type A, Glue Type C |
| Glue Station #3 | `GlueStation3` | Glue Type B, Glue Type C |
| Glue Station #4 | `GlueStation4` | Glue Type A, Glue Type B, Glue Type C |
| AGV | `Move` | Transport |
| Operator | `Source` | Pick-up, Drop |

### Product Variants

| Product | Plan |
| --- | --- |
| Product A | Pick-up, Glue Type A, Glue Type B, Drop |
| Product B | Pick-up, Glue Type A, Glue Type C, Drop |
| Product C | Pick-up, Glue Type A, Glue Type B, Glue Type C, Drop |

Students develop a multi-agent manufacturing system with three main agent types:

- Product Agent (PA): controls the execution flow for each product.
- Resource Agent (RA): abstracts shopfloor resources and exposes executable skills.
- Transport Agent (TA): coordinates AGV transport between locations.

The goal is to integrate these agents using the JADE framework and then extend the system with machine learning so it can predict station energy consumption and support lower-energy production decisions.

![Manufacturing System Overview](https://github.com/franciscoabadesantos/PredictionManufacturing/assets/65195331/5abeaa3b-61f8-4edb-8dbf-4118c35937ee)

During execution, the system collects station-level data such as skill performed and robot velocity. That data is used to train regression models capable of predicting the energy required to execute a specific skill at a specific station.

The prediction layer is intended to support questions such as:

- What is the expected energy required for `Glue_Type_A` at `GlueStation1`?
- Which station can execute the next step with lower energy cost?
- What velocity setting is associated with that predicted execution?

![Prediction Workflow](https://github.com/franciscoabadesantos/PredictionManufacturing/assets/65195331/67913da0-7eb2-42e5-a668-7ed981e2ad28)

## Repository Structure

```text
PredictionManufacturing/
├── applications/
│   ├── api/                  # FastAPI prediction service
│   └── control-system/       # JADE-based manufacturing control system
├── data/
│   ├── datasets/             # Station CSV datasets
│   └── models/               # Trained joblib models
├── notebooks/                # Training and experimentation notebooks
└── README.md
```

## Main Components

### `applications/api`

FastAPI service for exposing the trained prediction models.

- `GET /` returns a simple health check.
- `POST /prediction` returns energy and velocity predictions by station.

Example request body:

```json
{
  "Station": ["GlueStation1", "GlueStation3"],
  "Skill": "sk_g_a"
}
```

Run locally:

```bash
cd applications/api
uvicorn main:app --reload
```

### `applications/control-system`

Java/JADE multi-agent control system that manages:

- product orchestration
- resource negotiation
- transport coordination

Core source files are under `applications/control-system/src`.

## Data And Models

### `data/datasets`

CSV datasets for the four manufacturing stations.

### `data/models`

Saved regression models used by the API for:

- energy prediction
- velocity prediction

## Notebook

`notebooks/predictions.ipynb` contains the training and model selection workflow used to generate the saved models.
