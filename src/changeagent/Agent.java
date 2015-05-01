
/*
 *    Copyright 2014 -15 Joseph Harrington, all rights reserved.  No
 *     portion of this source may be used with express permission.
 *  
 *    Rev 1.2 March 2015.   
 *	1.	Replaced Constants interface with  AgentConstants class
 *	2.	Replaced Math.hypot with sqrt, serious performance hit 
 */
package changeagent;

import java.util.*;
import java.awt.*;
import java.awt.geom.*;
import static changeagent.AgentConstants.*;
import java.time.Duration;
import java.time.Instant;

/*
 *	agent constants.  colors may be set through UI in a future rev
 */
final class AgentConstants {

   static final int A_GROUP = 1;
   static final int B_GROUP = 2;
   static final Color AskinnyColor = new Color(127, 0, 0);
   static final Color AColor = new Color(255, 77, 76);
   static final Color AfatColor = new Color(255, 1, 1);
   static final Color BskinnyColor = new Color(38, 39, 127);
   static final Color BColor = new Color(76, 76, 255);
   static final Color BfatColor = new Color(1, 1, 255);
}

public class Agent extends Ellipse2D.Float {

   private final static ArrayList<Agent> agentList = new ArrayList<>();
   private final ArrayList<Agent> localAgentList;
   private final ArrayList<Agent> intersectList = new ArrayList<>();
   private final int agentGroup;
   private Color agentColor;
   private final float xLoc;
   private final float yLoc;
   private float deltaX;
   private float deltaY;

   private final ChangeAgent.StaticSystemRefs systemRefs = ChangeAgent.StaticSystemRefs.getInstance();
   private final Dimension frameDimension = systemRefs.getinsetFrameDimension();

   /*
    *	Size of the field is the area within which the agents move
    */
   private final int fieldWidth = frameDimension.width;
   private final int fieldHeight = frameDimension.height - systemRefs.getControlPanelHeight();

   /*
    *	The following fields represent state data common to all instances and mutable. Acknowledging 
    *	arguments against non-final static fields, the author chose to use private static declarations rather than 
    *	final wrapper class references out of an abundance of caution about performance costs (as noted in 
    *	Oracle documentation) of unboxing multiple wrapper classes multiple times in an animation loop.
    */
   private static float agentMaxSpeed = 3f;
   private static float agentMinSpeed = 0.2f;
   private static float heavySpeedFactor = 0.6f;
   private static float lightSpeedFactor = 1.5f;

   private static int numberOfAgents = 100;
   private static int startSize = 25; //40
   private static int minSize = 15; //20;
   private static int lightDelta = 3;
   private static int lightSize = minSize + lightDelta;
   private static int maxSize = 50;
   private static int heavyDelta = 5;
   private static int heavySize = maxSize - heavyDelta;
   private static int stateDelta = 1;

   private boolean heavyWeight = false;
   private boolean lightWeight = false;

   /*
    *	diameter is the state variable that is evaluated and updated in each invocation of update();
    *	super.width = super.height = diameter at the bottom of the agent interaction block
    */
   private float diameter = startSize;

   //	 these non-final statics should be put in a singleton class.  
   // Profile for cost to invoke singleton instance
   //	reference getters
   private static int A_aggressionRate = 50;
   private static int B_aggressionRate = 50;
   private static int A_propogateRate = 50;
   private static int B_propogateRate = 50;
   private final static Random random = new Random(); // used for random booleans

   /*
    *    for debugging intersectList management, 
    *    leave in for future development, easily broken
    *    private boolean fill = true;  
    */
   Instant startTime, nowTime;
   private Duration runTimeDuration;

   /*
    * The constructor sets the agent's initial location, deltaX, deltaY
    * speed (number of pixels moved per screen refresh) and group
    * assignment, all at random.  
    */
   public Agent(ArrayList<Agent> agentList) {

      super();
      this.localAgentList = agentList;
      startTime = Instant.now();

      /*
       * set  agent's color based on A_GROUP's propogation rate.
       * B_GROUP's propogation rate is 100 - APropogation rate
       */
      int propRandom = (int) (Math.random() * 100 + 1);

      if (propRandom <= A_propogateRate) {
         this.agentGroup = A_GROUP;
         this.agentColor = AColor;
      } else {
         this.agentGroup = B_GROUP;
         this.agentColor = BColor;
      }

      /*
       *    Genarate random starting location xLoc, yLoc
       */
      xLoc = (float) (Math.random() * (fieldWidth - diameter));
      yLoc = (float) (Math.random() * (fieldHeight - diameter));
      super.setFrame(xLoc, yLoc, diameter, diameter);

      /*
       *	Generate agent's' deltaX, deltaxY, random between minSpeed 
       *     and maxSpeed.
       *	TODO:	generate random bearing and calculate X and Y 
       *     components of speed for a given bearing.
       */
      deltaX = (float) ((Math.random() * (agentMaxSpeed - agentMinSpeed)) + (agentMinSpeed));
      if (random.nextBoolean() == true) {
         deltaX = -deltaX;
      }

      deltaY = (float) ((Math.random() * (agentMaxSpeed - agentMinSpeed)) + (agentMinSpeed));
      if (random.nextBoolean() == true) {
         deltaY = -deltaY;
      }

      float agentSpeed = (float) Math.sqrt(deltaY * deltaY + deltaX * deltaX);
   }   // close Agent() constructor

   /*
    *     this constructor resets newAgent flag false (default is true)
    *     used at system startup (AnimationThread() constructor)
    */
   public Agent(ArrayList<Agent> agentList, boolean newFlag) {
      this(agentList);
      this.newAgent = false;
   }

   /*
    *     This flag is check in PaintPanel.paintComponent().
    *     Consider making private with a getter, profile for 
    *     performance hit.
    */
   boolean newAgent = true;

   /*
    *    Agent.update(Agent  param)  is the workhorse, invoked from AnimationThread.run(),
    *     Iterates along the arrayList of Agent objects. The AnimationThread.run() loop is the
    *     "outer" loop.  An "inner" loop in this method checks every other agent in 
    *     the arrayListto see if the instant Agent intersects with it .  If true, the 
    *     Agent interaction takes place.  Agents less than minsize are removed, 
    *     after checking each Agent's intersectList.  
    */
   void update() {

      for (int i = 0; i < localAgentList.size() - 1; i++) {

         /*
          *    implements the fill/draw rendering cue for new agents.
          *    cue change at 5000 millis, set flag newAgent
          *    if not newAgent, skip time check.  
          *     profiled for max 200 agents, no significant difference.  
          */
         if (newAgent) {
            nowTime = Instant.now();
            runTimeDuration = Duration.between(startTime, nowTime);
            if (runTimeDuration.toMillis() > 5000) {
               newAgent = false;
            }
         }

         /*
          *    Reset colors to default.  After intersecting Agent processing loop, 
          *     Agent.size is check for heavyweight or lightWeight, then color 
          *     color is reset as appropriate
          */
         if (agentGroup == A_GROUP) {
            agentColor = AColor;
         } else {
            agentColor = BColor;
         }

         Agent innerLoopAgent = localAgentList.get(i);

         /*
          *  collison detection based on geometry
          */
         if (!innerLoopAgent.equals(this)
            && (innerLoopAgent.agentGroup != this.agentGroup)
            && contactCheck(innerLoopAgent)) {

            /* 
             * if innerAgent is already in this Agent's intersect list, continue from the top of the list.
             */
            if (this.intersectList.contains(innerLoopAgent)) {
               continue;
            } else {
               this.intersectList.add(innerLoopAgent);
            }

            /*
             *	debugging code to check if intersectList management is working
             *     leave it
             */
//				this.fill = false;
//				innerLoopAgent.fill = false;

            /*
             *	Execute Agent state change, according to 
             *     according to aggression rate  
             */
            if (this.agentGroup == A_GROUP) {
               /*
                * generate random int twixt 0 and value of A group aggression
                */
               if ((int) (Math.random() * 100 + 1) <= A_aggressionRate) { // A bites B
                  if (this.diameter < maxSize) {
                     this.diameter += stateDelta;
                  }
                  innerLoopAgent.diameter -= stateDelta;

               } else { // "this" is B_GROUP, B bites A
                  if (innerLoopAgent.diameter < maxSize) {
                     innerLoopAgent.diameter += stateDelta;
                  }
                  this.diameter -= stateDelta;
               } // close B bites A

            } // close this agent is A_GROUP
            else {
               /*
                * this.agentGroup == B_GROUP 
                * generate random int twixt 0 and value of B group aggression
                */
               if ((int) (Math.random() * 100 + 1) <= B_aggressionRate) { //B bites A
                  if (this.diameter < maxSize) {
                     this.diameter += stateDelta;
                  }
                  innerLoopAgent.diameter -= stateDelta;
               } // close B bites A
               else { // A bites B
                  if (innerLoopAgent.diameter < maxSize) {
                     innerLoopAgent.diameter += stateDelta;
                  }
                  this.diameter -= stateDelta;
               } // close A bites B
            }   // close this agent is B_GROUP

            /*
             *	reset this and innerLoopAgent Ellipse superclass fields height and width
             */
            super.height = super.width = this.diameter;
            innerLoopAgent.height = innerLoopAgent.width = innerLoopAgent.diameter;
         } // end if-intersect block
         /*
          *    The instant Agent and innerLoopAgent do not overlap. If innerLoopAgent 
          *     is in instant Agent's intersectList, remove it.
          *    TODO:	see if the ArrayList.remove() will work if the element is not a member, eliminate
          *    the if - contains() expression
          */ else {
            if (this.intersectList.contains(innerLoopAgent)) {
               this.intersectList.remove(innerLoopAgent);
            }

            /*
             *	Code for debugging the intersectList management.  
             *     Leave it for now.
             */
//				if (this.intersectList.isEmpty()) {
//					this.fill = true;
//				}
//				if (innerLoopAgent.intersectList.isEmpty()) {
//					innerLoopAgent.fill = true;
//				}
         }  // end if-intersect "else" block (no intersect)

         /*
          * reset colors for heavyweights and lightweights
          */
         if (this.diameter > heavySize && this.agentGroup == A_GROUP) {
            agentColor = AfatColor;
         } else if (this.diameter > heavySize && this.agentGroup == B_GROUP) {
            agentColor = BfatColor;
         } else if (this.diameter < lightSize && this.agentGroup == A_GROUP) {
            agentColor = AskinnyColor;
         } else if (this.diameter < lightSize && this.agentGroup == B_GROUP) {
            agentColor = BskinnyColor;
         }

         /*
          *    is the agent a heavyweight?  if it is not, was it on the last iteration?
          *    check boolean "heavyWeight" 
          *    TODO:	1. combine with above code which resets color, functions were 
          *     developed separately.  leave for now to get release out.
          *    2.    	AgentMenu class limits choices for speed factors to non-zero values.
          *    Should add check here for zero values, after evaluating performance.
          */
         if (this.diameter > heavySize && !heavyWeight) {
            deltaX = deltaX * heavySpeedFactor;
            deltaY = deltaY * heavySpeedFactor;
            heavyWeight = true;
         }
         if (this.diameter <= heavySize && heavyWeight) {
            deltaX = deltaX / heavySpeedFactor;
            deltaY = deltaY / heavySpeedFactor;
            heavyWeight = false;
         }
         if (this.diameter < lightSize && !lightWeight) {
            deltaX = deltaX * lightSpeedFactor;
            deltaY = deltaY * lightSpeedFactor;
            lightWeight = true;
         }
         if (this.diameter >= lightSize && lightWeight) {
            deltaX = deltaX / lightSpeedFactor;
            deltaY = deltaY / lightSpeedFactor;
            lightWeight = false;
         }
      } // close main for loop

      /* 
       *    cull the herd, take out agents that are too small, after removing 
       *    the agent from every other agent's ntersect list.  
       *    TODO: recode with enhanced for loop expressions.  refactor the innerloop
       */
      for (int j = 0; j < localAgentList.size(); j++) {
         Agent tempAgent = localAgentList.get(j);

         if (tempAgent.width < minSize) {
            // use isEmpty()
            for (Agent agentIterator : localAgentList) {
               if (agentIterator.intersectList.contains(tempAgent)) {
                  agentIterator.intersectList.remove(tempAgent);
               }
            }
            localAgentList.remove(tempAgent);
         }
      }

      super.x += deltaX;
      super.y += deltaY;

      // bounce off of right the wall
      if (super.x > fieldWidth - diameter) {
         super.x = fieldWidth - diameter;
         deltaX = -Math.abs(deltaX);
      }

      // bounce off of the left
      if (super.x < 0) {
         super.x = 0;
         deltaX = Math.abs(deltaX);
      }

      /*
       * bounce off of the bottom
       */
      if (super.y > fieldHeight - diameter) {
         super.y = fieldHeight - diameter;
         deltaY = -Math.abs(deltaY);
      }

      // bounce off of the top
      if (super.y < 0) {
         super.y = 0;
         deltaY = Math.abs(deltaY);
      }

   } // update()

   /*
    * intersectCheck()returns true if checkAgent is intersecting this agent.
    */
   private boolean contactCheck(Agent checkAgent) {
      Agent check = checkAgent;
      /*
       * calculate the distance between the two agent's  x and y coord-
       * inates.  if the distance is less than the sum of the two agents' radii
       * return true.
       */
      double differenceX = check.getCenterX() - this.getCenterX();
      double differenceY = check.getCenterY() - this.getCenterY();

      double sum = (differenceX * differenceX) + (differenceY * differenceY);
      double distance = Math.sqrt(sum);

      if (distance < (check.diameter / 2.0d + this.diameter / 2.0d)) {
         return (true);
      } else {
         return (false);
      }
   }

   /*
    * set  aggression rate
    */
   public static void setAggression(int group, int value) {
      if (group == A_GROUP) {
         A_aggressionRate = value;
         B_aggressionRate = 100 - value;
      } else if (group == B_GROUP) {
         B_aggressionRate = value;
         A_aggressionRate = 100 - value;
      }
   }

   /*
    * set propogate rate
    */
   public static void setPropagate(int group, int value) {
      if (group == A_GROUP) {
         A_propogateRate = value;
         B_propogateRate = 100 - value;
      } else if (group == B_GROUP) {
         B_propogateRate = value;
         A_propogateRate = 100 - value;
      }
   }

   public static int getNumberOfAgents() {
      return numberOfAgents;
   }

   public static void setNumberOfAgents(int number) {
      numberOfAgents = number;
   }

   public static ArrayList<Agent> getAgentList() {
      return agentList;
   }

   public static void setStartSize(int size) {
      startSize = size;
   }

   public static int getStartSize() {
      return startSize;
   }

   public static void setMinSize(int size) {
      minSize = size;
   }

   public static int getMinSize() {
      return minSize;
   }

   public static void setLightDelta(int size) {
      lightDelta = size;
      lightSize = minSize + lightDelta;
   }

   public static int getLightDelta() {
      return lightDelta;
   }

   public static void setLightSize(int size) {
      lightSize = size;
      lightDelta = lightSize - minSize;
   }

   public static int getLightSize() {
      return lightSize;
   }

   public static void setMaxSize(int size) {
      maxSize = size;
   }

   public static int getMaxSize() {
      return maxSize;
   }

   public static void setHeavyDelta(int size) {
      heavyDelta = size;
      heavySize = maxSize - heavyDelta;
   }

   public static int getHeavyDelta() {
      return heavyDelta;
   }

   public static void setHeavySize(int size) {
      heavySize = size;
      heavyDelta = maxSize - heavySize;
   }

   public static int getHeavySize() {
      return heavySize;
   }

   public static void setStateDelta(int size) {
      stateDelta = size;
   }

   public static int getStateDelta() {
      return stateDelta;
   }

   public static void setMinSpeed(float speed) {
      agentMinSpeed = speed;
   }

   public static float getMinSpeed() {
      return agentMinSpeed;
   }

   public static void setMaxSpeed(float speed) {
      agentMaxSpeed = speed;
   }

   public static float getMaxSpeed() {
      return agentMaxSpeed;
   }

   public static void setLightSpeedFactor(float factor) {
      lightSpeedFactor = factor;
   }

   public static void setHeavySpeedFactor(float factor) {
      heavySpeedFactor = factor;
   }

   public static float getLightSpeedFactor() {
      return lightSpeedFactor;
   }

   public static float getHeavySpeedFactor() {
      return heavySpeedFactor;
   }

   float getDeltaX() {
      return deltaX;
   }

   float getDeltaY() {
      return deltaY;
   }

   public float getSpeed() {
      return ((float) Math.sqrt(deltaY * deltaY + deltaX * deltaX));
   }

   public float getDiameter() {
      return diameter;
   }

   public int getAgentGroup() {
      return agentGroup;
   }

   public Color getAgentColor() {
      return agentColor;
   }

   /*
    *	development stub, unused
    */
   private class AgentSpeed {

      float deltaX;
      float deltaY;
      float maxAngle;

      AgentSpeed() {
         deltaX = 0.0f;
         deltaY = 0.0f;
         // maximum angle from horizontal is fieldHeight divided by one pixel, rather than infinity
         maxAngle = fieldHeight;
      }

      AgentSpeed getSpeed() {

         // tangent =  random value between 0 and nearly vertical fieldHeight / 1
         float tangent = (float) Math.random() * (fieldHeight);

         // angle from horizontal equals arctan(tangent)
         float angle = (float) Math.atan(tangent);
         // speed equals random between minSpeed and maxSpeed
         float speed = (float) Math.random() * (agentMaxSpeed - agentMinSpeed);
         // deltaX = arccos(angle * speed)
         deltaX = (float) Math.acos(angle * speed);
         // deltaY = arcsin(angle * speed);
         deltaY = (float) Math.asin(angle * speed);

         return this;
      }
   }

}
