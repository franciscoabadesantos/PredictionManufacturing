# PredictionManufacturing
 Machine Learning for Energy Consumption Prediction in a Manufacturing System

The system is constituted by four stations, particularly glue application workstations that apply structural adhesive to a product part. Additionally, there’s one Autonomous Ground Vehicle (AGV) responsible for moving the product between stations. Each resource/station is capable of performing the following operations/skills:


Table 1 - Skill distribution per Station

Resource/Station Location ID Skill ID

Glue Station #1 GlueStation1 Glue Type A, Glue Type B

Glue Station #2 GlueStation2 Glue Type A, Glue Type C

Glue Station #3 GlueStation3 Glue Type B, Glue Type C

Glue Station #4 GlueStation4 Glue Type A, Glue Type B, Glue Type C

AGV - Move

Operator Source Pick-up, Drop


The entry (pick-up) and removal (drop) of the product is performed by an operator (which can be abstracted as a resource). In this system, let us assume products are introduced in a given entry point. The AGV must move to the entry point (source), pick-up the product from the operator, then move it to an appropriate glue station. The glue station must be chosen based on the product requirements (its production process / plan) and their availability. Then, depending on the plan, it either moves the product to the following station or to an exit point (sink), through which the product leaves the system once more with the aid of the operator.


The product variants are described in Table 2:

Table 2 - Description of the product variants

Product Plan

Product A Pick-up, Glue Type A, Glue Type B, Drop

Product B Pick-up, Glue Type A, Glue Type C, Drop

Product C Pick-up, Glue Type A, Glue Type B, Glue Type C, Drop


In this assignment, students develop a multiagent system capable of controlling the production in a manner that is agile and flexible. In order to accomplish this, three generic agent types are proposed and described in Table 1. However, students are free to adapt this based on their modelling, as long as the overall functionality and characteristics of the system are ensured.

Product Agent (PA) - This agent is the entity responsible for controlling the entire execution of the process for a specific product in the job shop. Each product is abstracted by an agent of this type in a one-to-one relationship. For this, the PA should:

• Negotiate with the RAs which one should execute the next skill in the product’s execution list;

• Send a request to the TA to ask for transportation from its current location to the location of the RA chosen in the previous step;

• Send a request to the RA to perform the execution of the correct skill.

Resource Agent (RA) - This agent is responsible for abstracting a physical resource within the shopfloor, which depending on the level of granularity could be a robot, a gripper, or a station for example. Each resource is capable of executing a certain number of operations (represented in this assignment as skills), which can include for instance applying glue of a certain type, as required by the product’s execution list. Therefore, the RA should be capable of handling the product’s negotiation and request messages, as well as of performing the necessary skills using the appropriate library.

Transport Agent (TA) - The TA is the agent responsible for abstracting the AGV or AGVs that transport the products from point A to point B. Thus, the TA should handle the requests from PAs that require transportation to the location of a certain resource within the job shop. 

The goal of this assignment is to implement and integrate the PA, RA and TA required to control the job shop using the JADE framework.

![image](https://github.com/franciscoabadesantos/PredictionManufacturing/assets/65195331/5abeaa3b-61f8-4edb-8dbf-4118c35937ee)

This will encompass the development of a machine learning solution to automatically predict the energy consumption of executing a skill in a specific station. For example, what is the energy required to execute skill “Glue_Type_A” in the station “Glue_Station_1”? This system is depicted in Figure 1, following the same topology and execution logic already used in Lab 1

During the execution of each station, it is possible to collect the energy required for each execution, storing the skill performed and the robot velocity during execution. We have a file with this info for each station.
This data is stored in CSV format (Comma-separated values). 

The idea of the project is to create an API capable of predicting the energy required to execute one skill in one specific station and use that data to optimize the system, consuming less energy to produce each product.

![image](https://github.com/franciscoabadesantos/PredictionManufacturing/assets/65195331/67913da0-7eb2-42e5-a668-7ed981e2ad28)

In the initial stage, the focus will be on the implementation and training of the Regression Models, as well as on the server and respective service(s), which will host and provide the prediction functionality to the control system. Later, students should adapt the implementation of the control system to include the additional step of attempting to optimize energy consumption.
