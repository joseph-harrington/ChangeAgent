/*
 *    Copyright 2014 -15 Joseph Harrington, all rights reserved.  No
 *     portion of this source may be used with express permission.
 *  
 *    Rev 1.2 March 2015. 
 *
 *    1. Moved call to Agent update from paintComponent to
 *     ChangeAgent.AnimationThread.run()
 */
package changeagent;

import javax.swing.*;
import java.util.*;
import java.awt.*;
import java.time.Duration;
import java.time.Instant;

public class PaintPanel extends JPanel {

   private final ChangeAgent.StaticSystemRefs systemRefs
      = ChangeAgent.StaticSystemRefs.getInstance();
   private final ArrayList<Agent> agentList = Agent.getAgentList();

   public PaintPanel() {

      this.setOpaque(true);
      this.setBackground(Color.lightGray);
   }

   @Override
   public Dimension getPreferredSize() {

      Dimension dims = systemRefs.getinsetFrameDimension();
      int controlPanelHeight = systemRefs.getControlPanelHeight();
      return new Dimension(dims.width, dims.height - controlPanelHeight);
   }

   Instant nowTime;
   Duration duration;

   @Override
   public void paintComponent(Graphics g) {

      super.paintComponent(g);
      Graphics2D g2 = (Graphics2D) g;
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
         RenderingHints.VALUE_ANTIALIAS_ON);
      g2.setRenderingHint(RenderingHints.KEY_RENDERING,
         RenderingHints.VALUE_RENDER_SPEED);

      /*
       *    loop through arrayList, then get agent color and fill
       */
      g2.setStroke(new BasicStroke(3));
      for (int k = 0; k < agentList.size() - 1; k++) {
         g2.setColor(agentList.get(k).getAgentColor());

         /* 
          * this check implements the visual cue to show  new agents,
          *	they are drawn as circles for 5 sec, then filled
          */
         if (agentList.get(k).newAgent) {
            g2.draw(agentList.get(k));
         } else {
            g2.fill(agentList.get(k));

         }
      }
   }
}
