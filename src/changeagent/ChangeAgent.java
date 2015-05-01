/*
 *    Copyright 2014 -15 Joseph Harrington, all rights reserved.  No
 *     portion of this source may be used with express permission.
 *  
 *    Rev 1.2 March 2015.     Complete redesign from 1.1
 *     Add menubar, user interface to set/change agent parameters
 *     pause/restart
 */
package changeagent;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import java.util.concurrent.*;
import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import static changeagent.AgentConstants.*;

public class ChangeAgent {

   /*
    *	these system parameters are static because they are accessed 
    *  via static getters() from other classes.  control panel is set at a 
    *fixed height to give the animation panel maximum height in the display
    */
   private final static StaticSystemRefs systemRefs = StaticSystemRefs.getInstance();

   private static void createAndShowGUI() {

      int frameWidth;
      int frameHeight;
      Insets insets;
      int insetFrameWidth;
      int insetFrameHeight;

      /*
       *  instantiate rendering JPanel
       */
      PaintPanel pp = new PaintPanel();
      systemRefs.setPaintPanel(pp);

      /*
       *  instantiate JPanel containing system parameter
       *  controls, count and runtime clock
       */
      ControlPanel cp = new ControlPanel();
      systemRefs.setControlPanel(cp);
      int controlPanelHeight = systemRefs.getControlPanelHeight();

      /*
       * instantiate top level JFrame
       */
      JFrame frame = new JFrame(" ChangeAgent 1.2 ");

      GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
      frameWidth = env.getMaximumWindowBounds().width;
      frameHeight = env.getMaximumWindowBounds().height;

      frame.setSize(frameWidth, frameHeight);
      frame.setResizable(false);
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.getContentPane().setBackground(Color.yellow);
      frame.setLayout(null);
      frame.setJMenuBar(new AgentMenu(frame));

      /* 
       * set frame visible, get insets
       */
      frame.setVisible(true);
      insets = frame.getInsets();

      insetFrameWidth = frameWidth - insets.left - insets.right;
      insetFrameHeight = frameHeight - insets.top - insets.bottom;
      Dimension insetFrameDimension = systemRefs.getinsetFrameDimension();
      insetFrameDimension.width = insetFrameWidth;
      insetFrameDimension.height = insetFrameHeight;

      Dimension size = pp.getPreferredSize();
      pp.setBounds(0, 0, size.width, size.height);
      frame.add(pp);

      size = cp.getPreferredSize();
      cp.setBounds(0, insetFrameHeight - systemRefs.getControlPanelHeight(), size.width, size.height);
      frame.add(cp);

      StartDialog.showStartDialog(frame);

      startAgent();

   }  //createAndShowGUI

   public static void startAgent() {

      try {
         systemRefs.getThreadPoolExecutor().scheduleAtFixedRate(new AnimationThread(), 0L, 10L, TimeUnit.MILLISECONDS);
      } catch (Exception e) {
         System.exit(0);
      }
      systemRefs.setSuspendFlag(false);
      systemRefs.getControlPanel().clp.resumeClock();
   }

   public static void restart() {
      systemRefs.setSuspendFlag(false);
      systemRefs.getControlPanel().resumeClock();
   }

   public static void pause() {
      systemRefs.setSuspendFlag(true);
      systemRefs.getControlPanel().pauseClock();
   }

   /*
    *	 animation thread, invokes Agent.update, repaint
    */
   static class AnimationThread implements Runnable {

      private final StaticSystemRefs sr = StaticSystemRefs.getInstance();
      private final ControlPanel cp = sr.getControlPanel();
      private final PaintPanel pntp = sr.getPaintPanel();

      private final ArrayList<Agent> agentList = Agent.getAgentList();
      private final int numberAgents = Agent.getNumberOfAgents();
      private int oldRedCount = 0;
      private int oldBlueCount = 0;

      public AnimationThread() {

         for (int i = agentList.size() - 1; i < numberAgents; i++) {

            /*
             *	initialize the starting agentList, uses Agent constructor
             *     with booelan parameter for is a new agent?
             */
            Agent newAgent = new Agent(agentList, false);
            agentList.add(newAgent);
         }
      }

      /*
       *    Iterate through the arrayList of Agents, invoking Agent.update().  
       *    update() method also iterates through the arrayList, processing 
       *     the interaction between the instant Agent and every other Agent 
       *     in the ArrayList.
       *    Agent.update() may remove an item from the  arrayList.
       *    to prevent a null pointer exception the arrayList always has
       *    an extra Agent object at the end of the array.  this is why "for"
       *    loops iterate to agentList.size() - 1, rather than using an
       *    enhanced for loop
       */
      /*
       *    flag means first initialization, so that  the first set of agents
       *     at startup are not flagged as new.  Subsequent replacement
       *     agents are flagged and rendered as circles for 5 seconds to
       *     give visual indication that they are new.
       */
      @Override
      public void run() {

         try {
            if (!systemRefs.getSuspendFlag()) {

               /*
                *	populate the agentList, maintain number of agents
                */
               if (agentList.size() < numberAgents + 1) {
                  for (int i = agentList.size() - 1; i < numberAgents; i++) {
                     Agent newAgent = new Agent(agentList);
                     agentList.add(newAgent);
                  }
               }

               /*
                *	update Agents, Agent.update()
                */
               for (int k = 0; k < agentList.size() - 1; k++) {
                  agentList.get(k).update();
               }

               /*
                *    count the agents
                *    TODO fix bug, agent count is sometimes 1 less than number of
                *     agents
                */
               int redCount = 0;
               int blueCount = 0;
               for (int i = 0; i < agentList.size() - 1; i++) {
                  if (agentList.get(i).getAgentGroup() == A_GROUP) {
                     redCount++;
                  } else if (agentList.get(i).getAgentGroup() == B_GROUP) {
                     blueCount++;
                  }
               }

               /*
                *   if group count has changed, update display
                */
               if (oldRedCount != redCount) {
                  cp.setCountLables(redCount, blueCount);
               }
               oldRedCount = redCount;
               oldBlueCount = blueCount;

               /*
                *	repaint
                */
               pntp.repaint();
            }
         } catch (Exception e) {
            //	add popup error diaglogue
            System.exit(0);
         }
      }
   }

   public static void main(String[] args) {

      SwingUtilities.invokeLater(new Runnable() {
         @Override
         public void run() {
            createAndShowGUI();
         }
      });
   }

   /*
    *    A singleton class to hold global references used throught the 
    *     application, avoid non-final static references
    */
   public static class StaticSystemRefs {

      private static StaticSystemRefs instance;
      private PaintPanel paintPanel;
      private ControlPanel controlPanel;
      private final Dimension insetFrameDimension = new Dimension();
      private final int controlPanelHeight = 120;
      private boolean suspendFlag = false;
      private final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(2);

      private StaticSystemRefs() {
      }

      public static StaticSystemRefs getInstance() {
         if (instance == null) {
            instance = new StaticSystemRefs();
         }
         return instance;
      }

      public boolean getSuspendFlag() {
         return suspendFlag;
      }

      public void setSuspendFlag(boolean flag) {
         suspendFlag = flag;
      }

      public Dimension getinsetFrameDimension() {
         return insetFrameDimension;
      }

      public int getControlPanelHeight() {
         return controlPanelHeight;
      }

      public ScheduledThreadPoolExecutor getThreadPoolExecutor() {
         return executor;
      }

      void setPaintPanel(PaintPanel pp) {
         paintPanel = pp;
      }

      public PaintPanel getPaintPanel() {
         return paintPanel;
      }

      void setControlPanel(ControlPanel cp) {
         controlPanel = cp;
      }

      public ControlPanel getControlPanel() {
         return controlPanel;
      }
   }
}

/*
 *     consider moving to AgentMenu, with all other dialogue classes
 */
class StartDialog extends JDialog {

   private static JFrame owner;
   private static StartDialog startDialog;
   private final Font usefulFont = new Font("Helvetica", Font.BOLD, 18);
   private final ChangeAgent.StaticSystemRefs systemRefs = ChangeAgent.StaticSystemRefs.getInstance();

   private StartDialog(JFrame ownerFrame) {

      super(ownerFrame, "ChangeAgent", true);
      setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

      /*
       *	locate dialong near middle of application window
       */
      int frameWidth = systemRefs.getinsetFrameDimension().width;
      setLocation(frameWidth / 2, 100);

      JButton countButton = new JButton("Set Agent Count");
      countButton.setFont(usefulFont);
      countButton.addActionListener(
         new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
               AgentCountDialog.showAgentCountDialog(startDialog);
            }
         });

      JButton paramButton = new JButton("Set Parameters");
      paramButton.setFont(usefulFont);
      paramButton.addActionListener(
         new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
               ParamatersDialog.showParametersDialog(owner);
            }
         });

      JButton runButton = new JButton("Run");
      runButton.setFont(usefulFont);
      runButton.addActionListener(
         new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
               startDialog.setVisible(false);
            }
         });

      JButton cancelButton = new JButton("Cancel");
      cancelButton.setFont(usefulFont);
      cancelButton.addActionListener(
         new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
               System.exit(0);
            }
         });

      Container contentPane = this.getContentPane();
      JPanel buttonPanel = new JPanel();
      buttonPanel.setLayout(new BorderLayout());

      /*
       *	dialog layout needs improvement
       */
//		buttonPanel.add(Box.createVerticalGlue());
//		buttonPanel.add(Box.createRigidArea(new Dimension(0, 10)));
      buttonPanel.add(countButton, BorderLayout.WEST);
      buttonPanel.add(paramButton, BorderLayout.EAST);
      buttonPanel.add(runButton, BorderLayout.NORTH);
      buttonPanel.add(cancelButton, BorderLayout.SOUTH);

      contentPane.add(buttonPanel);
      pack();

   }

   static void showStartDialog(JFrame ownerFrame) {
      owner = ownerFrame;
      startDialog = new StartDialog(ownerFrame);
      startDialog.setVisible(true);
   }
}
