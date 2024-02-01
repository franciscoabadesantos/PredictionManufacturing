package Product;

import Product.ProductAgent.ItemResponse.StationData;
import Resource.ResourceAgent;
import jade.core.Agent;
import java.util.ArrayList;
import jade.core.behaviours.SequentialBehaviour;
import Utilities.DFInteraction;


import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;
import jade.core.AID;
import jade.core.behaviours.SimpleBehaviour;

import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAException;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREInitiator;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.apache.hc.client5.http.HttpHostConnectException;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;




/**
 *
 * @author Ricardo Silva Peres <ricardo.peres@uninova.pt>
 */
public class ProductAgent extends Agent {    
    
    String id;
    ArrayList<String> executionPlan = new ArrayList<>();
    // TO DO: Add remaining attributes required for your implementation
    String location="";
    
    @Override
    protected void setup() {
        Object[] args = this.getArguments();
        this.id = (String) args[0];
        this.executionPlan = this.getExecutionList((String) args[1]);
        System.out.println("Product launched: " + this.id + " Requires: " + executionPlan);
        
        SequentialBehaviour sequencia = new SequentialBehaviour();
        for(String skill:this.executionPlan){
            sequencia.addSubBehaviour(new resposta(this, createCFP(skill),skill));
            sequencia.addSubBehaviour(new WaitForConfirmBehaviour());      
        }
        this.addBehaviour(sequencia);
    }
    
    
 
    
    
        //envia mensagem e o oveeride dos métodos vai servir para a negociação de comunicação
        private class resposta extends ContractNetInitiator {

    
            String skill="";

        public resposta(Agent a, ACLMessage cfp,String skill) {
            super(a, cfp);
            this.skill=skill;
        }
                
        // este método é chamado quando as respostas forem recebidas ou o tempo acabar
        //Parametros: responses contains the responses received from the resource agents; acceptances is used by the initiator to send the acceptances or rejections of proposals to the resource agents.
        @Override
        protected void handleAllResponses(Vector responses, Vector acceptances) {
             // Create an empty vector to store accepted proposals
            Vector<ACLMessage> acceptedProposals = new Vector<>();
            List<String> acceptedAgentNames = new ArrayList<>();

            // Loop through all the responses
            for (Object responseObj : responses) {
                ACLMessage response = (ACLMessage) responseObj;
                if (response.getPerformative() == ACLMessage.PROPOSE) {
                    // Add the proposal to the vector of accepted proposals
                    acceptedProposals.add(response);

                    // Get the name of the agent that sent the proposal
                    String agentName = response.getSender().getLocalName();

                    // Add the agent name to the list of accepted agent names
                    acceptedAgentNames.add(agentName);
                }
            }
            
        int index=0;
    
         if( checkIfStringsExist(acceptedAgentNames)){
        // Create an instance of the Item class
        Item item = new Item(acceptedAgentNames,skill);
        item.setStation(acceptedAgentNames);
        item.setSkill(skill);

        // Serialize the Item object to JSON
        Gson gson = new GsonBuilder().create();
        String payload = gson.toJson(item);
        System.out.println( "\n\n\n payload: " +payload + "\n\n\n");

        MyClass myClass = new MyClass();
        try {
            // Call the sendRequest method
            String ChosenStation = myClass.sendRequest(payload);
  
            index = acceptedAgentNames.indexOf(ChosenStation);
            System.out.println( "index " +index );

        } catch (ParseException e) {
            e.printStackTrace();
        };

       
         }
                 
           
            // If there are no proposals, wait 5 sec and resend the cfp message
            if (acceptedProposals.isEmpty()) {
                System.out.println( myAgent.getLocalName()+": Nao houve propostas das estacoes, será enviado uma nova cpf dentro de 5 segundos");
                doWait(2000);
                addBehaviour(new resposta(this.myAgent, createCFP(skill),skill));
                return;
            }
               
            for (int i = 0; i <acceptedProposals.size(); i++) {

                 ACLMessage chosenOne = (ACLMessage) responses.elementAt(i);
                 ACLMessage sendMessage= chosenOne.createReply();
                 if(i==index){
                     sendMessage.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                 }
                 else{
                  sendMessage.setPerformative(ACLMessage.REJECT_PROPOSAL);                       
                 }
                 acceptances.addElement(sendMessage);     
            }
        }
        

        //este método vai esperar por uma resposta na forma de imformação 
        //o programa só poderá avançar assim que houver ovrride de todos os métodos
        @Override
        protected void handleInform(ACLMessage inform) {
            String[] partes = inform.getSender().getName().split("@");
            location = partes[0]; // "destino"
            System.out.println(myAgent.getLocalName()+ ": INFORM from CFP, message received with the following content: "+location);
            addBehaviour(new AGVComunication(this.getAgent(),createmsgforAGV(location,myAgent.getLocalName()),skill,location));        
        }
        

    };
        
        private class AGVComunication extends AchieveREInitiator {

            String skill="";
            String location="";


        public AGVComunication(Agent a, ACLMessage cfp,String skill,String location) {
            super(a, cfp);
            this.location=location;
            this.skill=skill;


        }
         
        @Override
        protected void handleInform(ACLMessage inform) {
            System.out.println(myAgent.getLocalName()+ ": INFORM  message received FROM: "+inform.getSender().getLocalName());
            this.myAgent.addBehaviour(new PerfomSkillComunication(this.myAgent, CreateRequestSkill(skill,location),skill));           
        }
        
        @Override
        protected void handleAgree(ACLMessage Agree) {
            System.out.println(myAgent.getLocalName()+ ": AGREE  message received FROM: "+Agree.getSender().getLocalName());
        }
                @Override
        protected void handleRefuse(ACLMessage refuse) {
            System.out.println(myAgent.getLocalName()+ ": REFUSE  message received FROM: "+refuse.getSender().getLocalName());
            System.out.println( myAgent.getLocalName()+ " esta ocupado neste momento será reenviado um novo pedido dentro de 5 segundos ");
            doWait(2000);
            addBehaviour(new AGVComunication(this.getAgent(),createmsgforAGV(location,myAgent.getLocalName()),skill,location));        
          
        }
          
    };
        
      private class PerfomSkillComunication extends AchieveREInitiator {
            String skill="";


        public PerfomSkillComunication(Agent a, ACLMessage cfp, String skill) {
            super(a, cfp);
            this.skill=skill;

        }
         
        @Override
        protected void handleInform(ACLMessage inform) {
            System.out.println(myAgent.getLocalName()+ ": INFORM  message received FROM: "+inform.getSender().getLocalName() +" Skill: "+skill);
            ACLMessage msg = new ACLMessage(ACLMessage.CONFIRM);
            AID receiver = new AID(myAgent.getLocalName(), AID.ISLOCALNAME);
            msg.addReceiver(receiver);
            msg.setContent(skill);
            myAgent.send(msg); 
        }
        
        @Override
        protected void handleAgree(ACLMessage Agree) {
            System.out.println(myAgent.getLocalName()+ ": AGREE  message received FROM: "+Agree.getSender().getLocalName());
        }
                @Override
        protected void handleRefuse(ACLMessage refuse) {
            System.out.println(myAgent.getLocalName()+ ": REFUSE  message received FROM: "+refuse.getSender().getLocalName());
            System.out.println( myAgent.getLocalName()+ ": A "+ refuse.getSender().getName() + "esta ocupado neste momento será reenviado um novo pedido dentro de 5 segundos ");
            doWait(2000);
            this.myAgent.addBehaviour(new PerfomSkillComunication(this.myAgent, CreateRequestSkill(skill,location),skill));           
          
        }
          
    };   
      
   public class WaitForConfirmBehaviour extends SimpleBehaviour {
    private boolean receivedConfirm = false;

    @Override
    public void action() {
        // Wait for CONFIRM message
        MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CONFIRM);
        ACLMessage msg = myAgent.receive(mt);
        if (msg != null) {
            receivedConfirm = true;
            System.out.println("Received CONFIRM message from " + msg.getSender().getName());
        } else {
            block();
        }
    }

    @Override
    public boolean done() {
        return receivedConfirm;
    }
}
         
    private ACLMessage CreateRequestSkill(String skill,String location){
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        System.out.println(" create a request to perfom a skill: "+ skill + " from: " + location+":");
        msg.addReceiver(new AID(location, AID.ISLOCALNAME));
        msg.setContent(skill);
        return msg;
    }

                  
    private ACLMessage createmsgforAGV(String Agent_name, String ProdutId){
        
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
       // cfp.setProtocol(ContractNetInitiator.CONTRACT_NET_PROTOCOL);
        // Set conversation ID or other properties if needed

        System.out.println(" Request to move from:" + Agent_name+":");
        msg.addReceiver(new AID("AGV", AID.ISLOCALNAME));
        String content =Agent_name+"@"+ ProdutId;//
        msg.setContent(content);
        return msg;
    }
    
    private ACLMessage createCFP(String step) {
        ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
       // cfp.setProtocol(ContractNetInitiator.CONTRACT_NET_PROTOCOL);
        cfp.setContent(step);
        // Set conversation ID or other properties if needed

        try {
            System.out.println(" Service:" + step+":");
            
            //Ver quais os agentes que podem fazer a tarefa pretendida
            DFAgentDescription[] result = DFInteraction.SearchInDFByType(step, this); //vetor com agentes

            for (DFAgentDescription agent : result) {
                //System.out.println(agent.getName() );

                cfp.addReceiver(agent.getName());   //inscrever os agentes para receber mensagem
            }
        } catch (FIPAException e) {
            // Para o caso extra de receber objeto que não usa nenhuma GlueStation
            Logger.getLogger(ResourceAgent.class.getName()).log(Level.SEVERE, null, e);

        }
        return cfp;
    }
      
    @Override
    protected void takeDown() {
        super.takeDown(); //To change body of generated methods, choose Tools | Templates.
    }
    
    private ArrayList<String> getExecutionList(String productType){
        switch(productType){
            case "A": return Utilities.Constants.PROD_A;
            case "B": return Utilities.Constants.PROD_B;
            case "C": return Utilities.Constants.PROD_C;
        }
        return null;
    }

    public class Item {
        private List<String> Station;
        private String Skill;

        public Item(List<String> station, String skill) {
            this.Station = station;
            this.Skill = skill;
        }

        public List<String> getStation() {
            return Station;
        }

        public void setStation(List<String> station) {
            this.Station = station;
        }

        public String getSkill() {
            return Skill;
        }

        public void setSkill(String skill) {
            this.Skill = skill;
        }
    }
    
    public class ItemResponse {
    private String status;
    private Map<String, StationData> predictions;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Map<String, StationData> getPredictions() {
        return predictions;
    }

    public void setPredictions(Map<String, StationData> predictions) {
        this.predictions = predictions;
    }

    public class StationData {
        private double Energy;
        private double Velocity;

        public double getEnergy() {
            return Energy;
        }

        public void setEnergy(double energy) {
            Energy = energy;
        }

        public double getVelocity() {
            return Velocity;
        }

        public void setVelocity(double velocity) {
            Velocity = velocity;
        }
    }
    
    }   

    public class MyClass {

        public String sendRequest(String payload) throws ParseException {
            String stationWithLowestEnergy = null;
            double lowestEnergy = Double.MAX_VALUE;
            String status = null;
            Map<String, StationData> predictions = null;

            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                // Create an HTTP POST request
                HttpPost httpPost = new HttpPost("http://127.0.0.1:8000/prediction");

                // Set the request headers
                httpPost.setHeader("Content-Type", ContentType.APPLICATION_JSON);

                // Set the request body
                String requestBody = payload;
                StringEntity requestEntity = new StringEntity(requestBody);
                httpPost.setEntity(requestEntity);

                // Execute the request
                CloseableHttpResponse response = httpClient.execute(httpPost);

                // Get the response entity
                HttpEntity responseEntity = response.getEntity();

                // Read the response content as a string
                String responseString = EntityUtils.toString(responseEntity);

                // Print the response
                System.out.println("/n/n/nResponse: " + responseString);

                // Parse the JSON response
                Gson gson_response = new GsonBuilder().create();
                ItemResponse itemResponse = gson_response.fromJson(responseString, ItemResponse.class);

                status = itemResponse.getStatus();
                predictions = itemResponse.getPredictions();

                response.close();
            } catch (HttpHostConnectException e) {
                System.err.println("Failed to connect to the server. Make sure the server is running.");
            } catch (IOException e) {
                e.printStackTrace();
            }


            if (status != null) {
                // Print the status
                System.out.println("Status: " + status);
            }

            if (predictions != null) {
                // Iterate over the predictions
                for (Map.Entry<String, StationData> entry : predictions.entrySet()) {
                    String station = entry.getKey();
                    StationData stationData = entry.getValue();

                    double energy = stationData.getEnergy();

                    if (energy < lowestEnergy) {
                        // Update the station with lowest energy
                        lowestEnergy = energy;
                        stationWithLowestEnergy = station;
                    }
                }
            }

            if (stationWithLowestEnergy != null) {
                // Print the station with lowest energy and its value
                System.out.println("Station with lowest energy: " + stationWithLowestEnergy);
                System.out.println("Lowest energy value: " + lowestEnergy);
            } else {
                // No station found in predictions
                System.out.println("No station found in predictions.");
            }

            // Return the station with lowest energy
            return stationWithLowestEnergy;

        }
    }

    
    public boolean checkIfStringsExist(List<String> acceptedAgentNames) {
        if (acceptedAgentNames.isEmpty()) {
            return false;
        }
        String searchString1 = "AVG";
        String searchString2 = "Operator";

        for (String agentName : acceptedAgentNames) {
            if (agentName.equals(searchString1) || agentName.equals(searchString2)) {
                return false;
            }
        }

        return true;
    }
}