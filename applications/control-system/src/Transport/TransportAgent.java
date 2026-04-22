package Transport;

import jade.core.Agent;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import Libraries.ITransport;
import Utilities.DFInteraction;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;
import java.util.HashMap;

/**
 *
 * @author Ricardo Silva Peres <ricardo.peres@uninova.pt>
 */
public class TransportAgent extends Agent {

    String id;
    ITransport myLib;
    String description;
    String[] associatedSkills;
    String Atual_position = "Source";
    boolean busy = false;
    HashMap<String, String> ProductPos = new HashMap<>();


    @Override
    protected void setup() {
        Object[] args = this.getArguments();
        this.id = (String) args[0];
        this.description = (String) args[1];

        //Load hw lib
        try {
            String className = "Libraries." + (String) args[2];
            Class cls = Class.forName(className);
            Object instance;
            instance = cls.newInstance();
            myLib = (ITransport) instance;
            System.out.println(instance);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(TransportAgent.class.getName()).log(Level.SEVERE, null, ex);
        }

        myLib.init(this);
        this.associatedSkills = myLib.getSkills();
        System.out.println("Transport Deployed: " + this.id + " Executes: " + Arrays.toString(associatedSkills));

        // TO DO: Register in DF
        try{
            DFInteraction.RegisterInDF(this, this.associatedSkills,  this.id);
        }
        catch(FIPAException ex){
            Logger.getLogger(TransportAgent.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        // TO DO: Add responder behaviour/s
        addBehaviour(new HandleMoveRequests(this,MessageTemplate.MatchPerformative(ACLMessage.REQUEST)));
        
    }
    
    @Override
    protected void takeDown() {
        super.takeDown();

    }
    
    private class HandleMoveRequests extends AchieveREResponder {

        public HandleMoveRequests(Agent a, MessageTemplate mt) {
            super(a, mt);
        }

        @Override
        
        protected ACLMessage handleRequest(ACLMessage request) throws NotUnderstoodException, RefuseException {
            System.out.println(myAgent.getLocalName()+ ": Processing Request message.");
            ACLMessage Agree_reply = request.createReply();
            if (!IsAGVbusy()){
                System.out.println( "Request accept");
                Agree_reply.setPerformative(ACLMessage.AGREE);
                Agree_reply.setContent(id);
                setAGVstate(true);

            }
            else{
                System.out.println( "Request Decline");
                Agree_reply.setPerformative(ACLMessage.REFUSE);
                Agree_reply.setContent(id);
                
            }
            
            return Agree_reply;
        }
        
        @Override
        
        protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) throws FailureException {
            String PosInical;
            String PosFinal;
            System.out.println(myAgent.getLocalName()+ ": Preparing Result of Request.");
            // get the location from the request
            String[] Destination = request.getContent().split("@");
            if("Operator".equals(Destination[0])){ Destination[0] = "Source";}
            
            if(checkProducExit(Destination[1])){
                PosInical = getposition(Destination[1]);
                PosFinal = Destination[0];     
            }
            else{
                PosInical ="Source";
                PosFinal = Destination[0];
            }
            
            System.out.println(myAgent.getLocalName()+ ": Vai comecar a mover-se na posição: "+PosInical +" até chegar: "+PosFinal+ ", transportando o produto: "+Destination[1]);
            //setAGVstate(true);
            myLib.executeMove(PosInical, PosFinal, Destination[1]); 
            //block(1000);
            setAGVstate(false);
            
            SetNewPos(Destination[1],PosFinal);

            // make a repay
            ACLMessage reply = request.createReply();
            reply.setPerformative(ACLMessage.INFORM);
            reply.setContent(id);
            return reply;
        }  
        
        public String get_Currrent_Position(){
            return Atual_position;
        }
        public boolean IsAGVbusy(){
            return TransportAgent.this.busy;
        }
        public void setAGVstate(boolean status){
            
            TransportAgent.this.busy=status;
        }
        
        public boolean checkProducExit(String chave){
            return ProductPos.containsKey(chave);
        }
        public String getposition(String chave){
            return ProductPos.get(chave);       
        }
        public void SetNewPos(String chave,String PosFinal){
            ProductPos.put(chave, PosFinal);
            
        }

    }
}
