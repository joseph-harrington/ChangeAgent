/*
 *    Copyright 2014 -15 Joseph Harrington, all rights reserved.  No
 *     portion of this source may be used with express permission.
 *  
 *    Rev 1.2 March 2015. 

 *		1.	Change to using parameter defined anonymous class MouseEvent handlers.  
 *		In version 1.1, event handler subclasses processed JSlider events with sequence of 
 *		if - else if statements.  
 *		2.	get clock thread from pool instantiated in ChangeAgent class
 */
package changeagent;

import java.awt.Color;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.time.Duration;
import java.time.Instant;
import static changeagent.AgentConstants.*;
import java.util.concurrent.TimeUnit;

public class ControlPanel extends JPanel {

   private final ChangeAgent.StaticSystemRefs systemRefs
      = ChangeAgent.StaticSystemRefs.getInstance();
   private final int controlPanelHeight = systemRefs.getControlPanelHeight();

   private final int sliderPanelWidth = 500;
   private final float sliderPanelScale = 0.7f;
   private JSlider aggressionSliderA;
   private JSlider aggressionSliderB;
   private JSlider propogationSliderA, propogationSliderB;
//   private Dashboard db;
   private Dashboard2 db2;
   private boolean dashboardUp = false;
   private final CountPanel cntp = new CountPanel();
   final ClockPanel clp = new ClockPanel();

   ControlPanel() {

      TitledBorder tb = borderTitle("");
      this.setBorder(tb);

      this.setOpaque(true);
      this.setBackground(Color.gray);

      this.add(new AggressionSliderPanel());
      this.add(new PropagateSliderPanel());

      this.add(cntp);

      this.add(clp);
      ClockPanel.ClockThread clt = clp.new ClockThread();
      systemRefs.getThreadPoolExecutor().scheduleAtFixedRate(clt, 0L, 1000L, TimeUnit.MILLISECONDS);

      JButton dashBoardButton = new JButton();
      Font font = new Font("Serif", Font.ITALIC | Font.BOLD, 12);
      dashBoardButton.setFont(font);
      dashBoardButton.setText("Dashboard");
      dashBoardButton.addMouseListener(new MouseAdapter() {
         @Override
         public void mousePressed(MouseEvent e) {
            if (!dashboardUp) {
               if (e.getButton() == 1) {
                  db2 = new Dashboard2();
                  dashboardUp = true;
               }
            } else if (dashboardUp) {
               db2.shutDown();
               dashboardUp = false;
            }
         }
      });
      this.add(dashBoardButton);
   }

   @Override
   final public Dimension getPreferredSize() {
      Dimension dims = systemRefs.getinsetFrameDimension();
      int controlPanelHeight = systemRefs.getControlPanelHeight();
      return new Dimension(dims.width, controlPanelHeight);
   }

   static TitledBorder borderTitle(String title) {
      Font font = new Font("Serif", Font.ITALIC | Font.BOLD, 18);
      Border commonBorder = BorderFactory.createEtchedBorder(Color.black, Color.DARK_GRAY);
      TitledBorder tb = new TitledBorder(commonBorder, title,
         TitledBorder.CENTER, TitledBorder.TOP, font);

      return tb;
   }

   void setDashboardFlag(boolean flag) {
      dashboardUp = flag;
   }

   void setCountLables(int ACount, int BCount) {

      int x = ACount;
      int y = BCount;
      cntp.updateCountLabels(x, y);
   }

   /*
    *     called from AgentMenu event handler
    */
   void pauseClock() {
      clp.pauseClock();
   }

   void resumeClock() {
      clp.resumeClock();
   }

   /*
    *    this panel contains two sliders for adjusting aggression 
    *	REFACTOR these 
    */
   private class AggressionSliderPanel extends JPanel {

      AggressionSliderPanel() {

         TitledBorder tb = borderTitle("Agent Aggression");
         this.setBorder(tb);
         this.setBackground(Color.lightGray);
         int scaledHeight = (int) (sliderPanelScale * controlPanelHeight);
         this.setPreferredSize(new Dimension(sliderPanelWidth, scaledHeight));

         aggressionSliderA = new SystemSlider(AColor);
         aggressionSliderB = new SystemSlider(BColor);

         aggressionSliderA.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
               if (e.getButton() == 1) {
                  int value = aggressionSliderA.getValue();
                  aggressionSliderB.setValue(100 - value);
               } else if (e.getButton() == 3) {
                  aggressionSliderA.setValue(50);
                  aggressionSliderB.setValue(50);
               }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
               int value = aggressionSliderA.getValue();
               aggressionSliderB.setValue(100 - value);
               Agent.setAggression(A_GROUP, value);
            }
         });
         aggressionSliderA.addMouseMotionListener(new MouseMotionAdapter() {
            int value;

            @Override
            public void mouseDragged(MouseEvent me) {
               value = aggressionSliderA.getValue();
               aggressionSliderB.setValue(100 - value);
            }
         });

         aggressionSliderB.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
               if (e.getButton() == 1) {
                  int value = aggressionSliderB.getValue();
                  aggressionSliderA.setValue(100 - value);
               } else if (e.getButton() == 3) {
                  aggressionSliderA.setValue(50);
                  aggressionSliderB.setValue(50);
               }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
               int value = aggressionSliderB.getValue();
               aggressionSliderA.setValue(100 - value);
               Agent.setAggression(B_GROUP, value);
            }
         });
         aggressionSliderB.addMouseMotionListener(new MouseMotionAdapter() {
            int value;

            @Override
            public void mouseDragged(MouseEvent me) {
               value = aggressionSliderB.getValue();
               aggressionSliderA.setValue(100 - value);
            }
         });

         this.add(aggressionSliderA);
         this.add(aggressionSliderB);

      }
   }  // close class AggressionSliderPanel

   /*
    *    this panel contains two sliders for adjusting propogation rate
    */
   private class PropagateSliderPanel extends JPanel {

      PropagateSliderPanel() {

         TitledBorder tb = borderTitle("Agent Propagation");
         this.setBorder(tb);
         this.setBackground(Color.lightGray);
         int scaledHeight = (int) (sliderPanelScale * controlPanelHeight);
         this.setPreferredSize(new Dimension(sliderPanelWidth, scaledHeight));

         propogationSliderA = new SystemSlider(AColor);
         propogationSliderB = new SystemSlider(BColor);

         propogationSliderA.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
               if (e.getButton() == 1) {
                  int value = propogationSliderA.getValue();
                  propogationSliderB.setValue(100 - value);
//                         System.out.println("anonymouse class prop slider A  pressed " + value);
               } else if (e.getButton() == 3) {
                  propogationSliderA.setValue(50);
                  propogationSliderB.setValue(50);
               }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
               int value = propogationSliderA.getValue();
               propogationSliderB.setValue(100 - value);
               Agent.setPropagate(A_GROUP, value);
            }
         });

         propogationSliderB.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
               if (e.getButton() == 1) {
                  int value = propogationSliderB.getValue();
                  propogationSliderA.setValue(100 - value);
               } else if (e.getButton() == 3) {
                  propogationSliderA.setValue(50);
                  propogationSliderB.setValue(50);
               }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
               int value = propogationSliderB.getValue();
               propogationSliderA.setValue(100 - value);
               Agent.setPropagate(B_GROUP, value);
            }
         });

         propogationSliderA.addMouseMotionListener(new MouseMotionAdapter() {
            int value;

            @Override
            public void mouseDragged(MouseEvent me) {
               value = propogationSliderA.getValue();
               propogationSliderB.setValue(100 - value);
            }
         });

         propogationSliderB.addMouseMotionListener(new MouseMotionAdapter() {
            int value;

            @Override
            public void mouseDragged(MouseEvent me) {
               value = propogationSliderB.getValue();
               propogationSliderA.setValue(100 - value);
            }
         });

         this.add(propogationSliderA);
         this.add(propogationSliderB);
      }

   }  // close class PropagateSliderPanel

   private class CountPanel extends JPanel {

      JLabel groupACountLabel;
      JLabel groupBCountLabel;

      CountPanel() {

         TitledBorder tb = borderTitle("Count");
         this.setBorder(tb);

         this.setBackground(Color.lightGray);

         int scaledHeight = (int) (sliderPanelScale * controlPanelHeight);
         this.setPreferredSize(new Dimension(100, scaledHeight));

         Font font = new Font("Serif", Font.ITALIC | Font.BOLD, 18);

         groupACountLabel = new JLabel();
         groupACountLabel.setFont(font);
         groupACountLabel.setForeground(AColor);
         groupACountLabel.setText("Red ");

         groupBCountLabel = new JLabel();
         groupBCountLabel.setFont(font);
         groupBCountLabel.setForeground(BColor);
         groupBCountLabel.setText("Blue ");

         this.add(groupACountLabel);
         this.add(groupBCountLabel);

      }

      void updateCountLabels(int groupA, int groupB) {
         groupACountLabel.setText("Red " + groupA);
         groupBCountLabel.setText(" Blue " + groupB);
      }
   }

    class ClockPanel extends JPanel {

      private final JLabel clockLabel;
      private final Instant startTime;
      private Instant pauseStart;
      private Duration cumulativePauseDuration;
      private boolean runClock = false;

      ClockPanel() {

         TitledBorder tb = borderTitle("Runtime");
         this.setBorder(tb);
         this.setBackground(Color.lightGray);

         int scaledHeight = (int) (sliderPanelScale * controlPanelHeight);
         this.setPreferredSize(new Dimension(100, scaledHeight));
         Font font = new Font("Serif", Font.ITALIC | Font.BOLD, 18);

         clockLabel = new JLabel();
         clockLabel.setFont(font);
         clockLabel.setText("Timer");
         this.add(clockLabel);

         startTime = Instant.now();
         pauseStart = startTime;
         cumulativePauseDuration = Duration.between(startTime, startTime);
      }

      /*
       *     pause and resume called from AgentMenu menu item
       *     event handler
       */
      public void resumeClock() {
         Duration lastPauseDuration = Duration.between(pauseStart, Instant.now());
         cumulativePauseDuration = cumulativePauseDuration.plus(lastPauseDuration);
         runClock = true;
      }

      public void pauseClock() {
         pauseStart = Instant.now();
         runClock = false;
      }

      void updateClock() {

         Instant nowTime;
         Duration runTimeDuration;

         nowTime = Instant.now();
         runTimeDuration = Duration.between(startTime, nowTime);
         runTimeDuration = runTimeDuration.minus(cumulativePauseDuration);

         /*
          *	parse runTimeDuration and build output string
          */
         String runTimeSecondsString;
         long runTimeSeconds = runTimeDuration.toMillis() / 1000 % 60;
         if (runTimeSeconds < 10) {
            runTimeSecondsString = ":0" + runTimeSeconds;
         } else {
            runTimeSecondsString = ":" + runTimeSeconds;
         }

         String minutesString;
         long runTimeMinutes = runTimeDuration.toMinutes() % 60;
         if (runTimeMinutes == 0) {
            minutesString = ":00";
         } else if (runTimeMinutes < 10) {
            minutesString = ":0" + runTimeMinutes;
         } else {
            minutesString = ":" + runTimeMinutes;
         }

         String runTimeHoursString;
         long runTimeHours = runTimeDuration.toHours();
         if (runTimeHours == 0) {
            runTimeHoursString = "00";
         } else if (runTimeHours > 0 && runTimeHours < 10) {
            runTimeHoursString = "0" + runTimeHours;
         } else {
            runTimeHoursString = " " + runTimeHours;
         }

         timeString = runTimeHoursString + minutesString + runTimeSecondsString;
         clockLabel.setText(timeString);

      }

      String timeString;

      public String getTimeString() {
         return timeString;
      }

      public class ClockThread implements Runnable {

         @Override
         public void run() {
            if (runClock) {
               updateClock();
            }
         }
      }
   }

   private class SystemSlider extends JSlider {

       SystemSlider(Color sliderColor) {

         Font font = new Font("Serif", Font.ITALIC | Font.BOLD, 12);

         this.setFont(font);
         this.setBackground(sliderColor);
         this.setForeground(Color.black);
         this.setMinimum(0);
         this.setMaximum(100);
         this.setValue(50);

         Dimension dimension = new Dimension();
         dimension.height = 40;
         dimension.width = 230;
         this.setPreferredSize(dimension);
         this.setMajorTickSpacing(10);
         this.setPaintTicks(true);
         this.setPaintLabels(true);
      }
   }
}  //close Control Panel class
