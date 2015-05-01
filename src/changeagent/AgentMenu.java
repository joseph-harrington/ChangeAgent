/*
 *    Copyright 2014 -15 Joseph Harrington, all rights reserved.  No
 *     portion of this source may be used with express permission.
 *  
 *    Rev 1.2 March 2015. 

 *	ChangeAgent Menu bar and dialogs
 */
package changeagent;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.geom.Ellipse2D;
import javax.swing.border.Border;

/**
 *
 * @author Joseph
 */
public class AgentMenu extends JMenuBar {

   private final JFrame owner;
   private final Font font = new Font("Helvetica", Font.BOLD, 16);
   private final Font Itemfont = new Font("Helvetica", Font.BOLD, 18);
   private boolean aboutFlag = false;

   AgentMenu(JFrame frame) {
      owner = frame;
      createMenuBar();
   }

   final void createMenuBar() {

      JMenuItem menuItem;
      this.setFont(font);

      JMenu fileMenu = new JMenu("File");
      fileMenu.setMnemonic(KeyEvent.VK_F);
      fileMenu.setFont(font);
      this.add(fileMenu);

      menuItem = new JMenuItem("Open", KeyEvent.VK_O);
      menuItem.setFont(Itemfont);
      menuItem.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            ChangeAgent.pause();
         }
      });
      menuItem.setEnabled(false);
      fileMenu.add(menuItem);

      menuItem = new JMenuItem("Pause", KeyEvent.VK_N);
      menuItem.setFont(Itemfont);
      menuItem.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            ChangeAgent.pause();
         }
      });
      fileMenu.add(menuItem);

      menuItem = new JMenuItem("Resume", KeyEvent.VK_N);
      menuItem.setFont(Itemfont);
      menuItem.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            ChangeAgent.restart();
         }
      });
      fileMenu.add(menuItem);

      menuItem = new JMenuItem("Exit", KeyEvent.VK_N);
      menuItem.setFont(Itemfont);
      menuItem.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            System.exit(0);
         }
      });
      fileMenu.add(menuItem);

      /*
       *	Settings menu
       */
      JMenu settingsMenu = new JMenu("Settings");
      settingsMenu.setMnemonic(KeyEvent.VK_S);
      settingsMenu.setFont(font);
      this.add(settingsMenu);

      /*
       *	Menu choice to number of agents at start
       *     Disabled in 1.2, agent number may be set from main 
       *     settings dialogue
       */
      menuItem = new JMenuItem("Number of Agents");
      menuItem.setFont(Itemfont);
      menuItem.addActionListener(new ActionListener() {

         @Override
         public void actionPerformed(ActionEvent e) {
//				ParamatersDialog.showDialog(owner);
         }
      });
      menuItem.setEnabled(false);
      settingsMenu.add(menuItem);

      /*
       *	Set system parameters: sizes, speeds
       */
      menuItem = new JMenuItem("System Parameters");
      menuItem.setFont(Itemfont);

      menuItem.addActionListener(new ActionListener() {

         @Override
         public void actionPerformed(ActionEvent e) {
            ParamatersDialog.showParametersDialog(owner);
         }
      });
      settingsMenu.add(menuItem);

      /*
       *	About menu
       */
      JMenu aboutMenu = new JMenu("About");
      aboutMenu.setMnemonic(KeyEvent.VK_A);
      aboutMenu.setFont(font);
      aboutMenu.addMenuListener(new MenuListener() {

         @Override
         public void menuSelected(MenuEvent e) {
            if (!aboutFlag) {
               AboutDialog.showAboutDialog(owner);
               aboutFlag = true;
            }
         }

         @Override
         public void menuDeselected(MenuEvent e) {
            AboutDialog.removeAboutDialog();
            aboutFlag = false;
         }

         @Override
         public void menuCanceled(MenuEvent e) {
         }
      });
      this.add(aboutMenu);

   }

   private static class AboutDialog extends JDialog {

      private static AboutDialog ad;
      private final Font usefulFont = new Font("Helvetica", Font.BOLD, 18);

      private AboutDialog(Frame owner) {

         super(owner, "ChangeAgent", false);
         setLocationRelativeTo(owner);
         this.setPreferredSize(new Dimension(600, 200));

         JTextArea jta = new JTextArea(description);
         jta.setLineWrap(true);
         jta.setWrapStyleWord(true);
         jta.setFont(usefulFont);

         JScrollPane jsp = new JScrollPane(jta,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

         this.add(jsp, BorderLayout.CENTER);
         JButton okButton = new JButton("OKAY");
         okButton.setFont(usefulFont);
         okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
               setVisible(false);
            }
         });
         this.add(okButton, BorderLayout.PAGE_END);
         pack();
      }

      static void showAboutDialog(JFrame owner) {
         ad = new AboutDialog(owner);
         ad.setVisible(true);
      }

      static void removeAboutDialog() {
         ad.setVisible(false);
      }

      String description = "ChangeAgent is a dynamic system simulation. "
         + "Autonomous agents are modelled as filled circles.  When two agents of different "
         + "type make contact, one agent’s “sphere of influence\" "
         + "grows and the other is diminished.  When an agent's "
         + "size is reduced to the a minimum size, that agent is "
         + "removed and replaced with a new agent.  Color cues indicate when "
         + "an agent is close to either the maximum or minimum end of the size range."
         + " By default, all interactions and group assignments are random."
         + " Use the controls at the bottom to adjust aggression and propagation rates, "
         + " then observe the effect on each group's population and size distribution."
         + "Manipulate the system parameters to change the groups' populations, then"
         + " attempt to restore the system to equal size groups";
   }
}	// class AgentMenu

/*
 *	Invoked from ChangeAgent.StartDialog()
 */
class AgentCountDialog extends JDialog {

   private static AgentCountDialog acd;
   private int numberOfAgents = Agent.getNumberOfAgents();
   private final Font usefulFont = new Font("Helvetica", Font.BOLD, 18);
   private final Border usefulBorder = BorderFactory.createLineBorder(Color.darkGray, 2);

   private AgentCountDialog(StartDialog owner) {
      super(owner, "Set Agent Count", true);
      Insets insets = owner.getInsets();
      setLocationRelativeTo(owner);
      Container contentPane = this.getContentPane();
      contentPane.setLayout(new BorderLayout());

      contentPane.add(numberChoicePanel(), BorderLayout.PAGE_START);
      pack();
   }

   public static void showAgentCountDialog(StartDialog owner) {
      acd = new AgentCountDialog(owner);
      acd.setVisible(true);
   }

   private JPanel numberChoicePanel() {

      /*
       *	number of agents.  Editable combo box
       */
      String[] numberOfAgentsCombxString = {"50", "60", "70", "80", "90", "100"};
      int defaultIndex = 5;
      JComboBox<String> numberOfAgentsCombx = new JComboBox<>(numberOfAgentsCombxString);
      numberOfAgentsCombx.setSelectedIndex(defaultIndex);
      numberOfAgentsCombx.setFont(usefulFont);
      numberOfAgentsCombx.setEditable(true);

      numberOfAgentsCombx.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            JComboBox cmb = (JComboBox) e.getSource();
            String choice = (String) cmb.getSelectedItem();
            numberOfAgents = Integer.parseInt(choice);
         }
      });

      JLabel numberLabel = new JLabel();
      numberLabel.setOpaque(true);
      numberLabel.setFont(usefulFont);
      numberLabel.setText("Number of Agents");

      /*
       *	Set Parameters button
       */
      JButton setButton = new JButton("Set Agent Count");
      setButton.setFont(usefulFont);

      setButton.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            Agent.setNumberOfAgents(numberOfAgents);
            setVisible(false);
         }
      });

      JPanel numberPanel = new JPanel();
      numberPanel.setLayout(new BorderLayout());
//		numberPanel.add(componentPanel, LEFT_ALIGNMENT);
      numberPanel.add(numberOfAgentsCombx, BorderLayout.NORTH);
      numberPanel.add(setButton, BorderLayout.SOUTH);
      numberPanel.validate();
      return numberPanel;
   }

}

/*
 *	A dialog for the user to select Agent size parameters, expressed in pixels.  Options range from 
 *	15 to 60.
 */
class ParamatersDialog extends JDialog {

   private static ParamatersDialog dialog;
   private final Font usefulFont = new Font("Helvetica", Font.BOLD, 18);
   private final Border usefulBorder = BorderFactory.createLineBorder(Color.darkGray, 2);
   private boolean paramErrorFlag = false;
   private final ChangeAgent.StaticSystemRefs systemRefs = ChangeAgent.StaticSystemRefs.getInstance();

   /*
    *	private constructor called only by showDialog();
    */
   private ParamatersDialog(JFrame frame) {

      super(frame, "Set Parameters", true);

      /*
       *	position dialog near the center of the application window
       */
      Insets insets = frame.getInsets();
      int frameWidth = systemRefs.getinsetFrameDimension().width;
      setLocation(frameWidth / 2, 100);

      JTabbedPane tabs = new JTabbedPane();
      tabs.setFont(usefulFont);
      tabs.addTab("Size", sizePanel());
      tabs.addTab("Speed", speedPanel());

      Container contentPane = this.getContentPane();
      contentPane.setLayout(new BorderLayout());
      contentPane.add(tabs, BorderLayout.PAGE_START);

      /*
       *	Set Parameters button
       */
      JButton setButton = new JButton("Set  Parameters");
      setButton.setFont(usefulFont);

      setButton.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            if (!paramErrorFlag) {
               setAgentParamters();
               setVisible(false);
            } else {
               ErrorDialog.showErrorDialog(dialog, "Incorrect parameter settings");
            }
         }
      });

      JButton cancelButton = new JButton("Cancel");
      cancelButton.setFont(usefulFont);
      cancelButton.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            setVisible(false);
         }
      });

      /*
       *	JPanel for the DONE and CANCEL buttons
       */
      JPanel buttonPanel = new JPanel();
      buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
      buttonPanel.add(Box.createHorizontalGlue());
      buttonPanel.add(setButton);
      buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
      buttonPanel.add(cancelButton);

      contentPane.add(buttonPanel, BorderLayout.PAGE_END);
      pack();
   }

   /*
    *	showParametersDialog() called by JMenuItem "System Parameters" event handler
    */
   public static void showParametersDialog(JFrame ownerFrame) {

      dialog = new ParamatersDialog(ownerFrame);
      dialog.setVisible(true);

   }

   /*	
    *	A dialog to warn of incompatible parameter choices.  showErrorDialog() Invoked by system
    *	parameter JComboBox actionListeners.
    */
   private static class ErrorDialog extends JDialog {

      private static ErrorDialog ed;
      private final Font errorFont = new Font("Helvetica", Font.BOLD, 18);

      public static void showErrorDialog(ParamatersDialog owner, String msg) {
         ed = new ErrorDialog(owner, msg);
         ed.setVisible(true);
      }

      /*
       *	private constructor called by showErrorDialog()
       */
      private ErrorDialog(ParamatersDialog owner, String msg) {

         super(owner, "Set Parameters", true);
         setLocationRelativeTo(owner);

         JLabel errorLabel = new JLabel(msg);
         errorLabel.setFont(errorFont);
         errorLabel.setHorizontalAlignment(SwingConstants.CENTER);

         Graphics gc = owner.getGraphics();
         FontMetrics fmet = gc.getFontMetrics(errorFont);
         int stringWidth = fmet.stringWidth(msg);
         int stringHeight = fmet.getHeight();
         errorLabel.setPreferredSize(new Dimension(stringWidth + 10, stringHeight));

         JButton okButton = new JButton("OKAY");
         okButton.setFont(errorFont);
         int buttonWidth = fmet.stringWidth("OKAY");
         okButton.setPreferredSize(new Dimension(buttonWidth, 50));

         okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
//               System.out.println("OKAY button pushed");
               ed.setVisible(false);
            }
         });

         JPanel pane = new JPanel();
         pane.setLayout(new BorderLayout());
         pane.add(errorLabel, BorderLayout.PAGE_START);
         pane.add(okButton, BorderLayout.PAGE_END);

         Container contentPane = this.getContentPane();
         contentPane.add(pane);
         pack();
      }

   }

   /*
    *    A class for JComboBoxes(String[], int) constructors.  
    *     comboString is the array of strings parameter,
    *    defaultIndex is the parameter for setSelectedIndex()
    */
   static class ComboParams {

      String[] comboString;
      int defaultIndex;
   }

   /*
    *    A method to generate the JComboBox parameters 
    *    (String[] comboString, int defaultIndex). Takes (int low, int high, int default).
    *    Compose an array of integer strings ranging from the
    *     low to the high values,  sets the default array index to
    *     correspond to the default value.
    */
   private static ComboParams getComboParams(int low, int high, int defaultValue) {

      int numberChoices = high - low + 1;
      int startDefaultCounter = low;
      int defaultIndex = 0;

      String[] comboString = new String[numberChoices];
      for (int i = 0; i < numberChoices; i++) {
         if (startDefaultCounter == defaultValue) {
            defaultIndex = i;
         }
         comboString[i] = Integer.toString(startDefaultCounter++);
      }

      ComboParams comboParams = new ComboParams();
      comboParams.comboString = comboString;
      comboParams.defaultIndex = defaultIndex;
      return comboParams;
   }

   /*
    *	these members are class fields so they can be accessed by anonymous inner class event handlers
    */
   private int startSize = Agent.getStartSize();
   private int minSize = Agent.getMinSize();
   private int lightSizeDelta = Agent.getLightDelta();
   private int lightSize = Agent.getLightSize();
   private int maxSize = Agent.getMaxSize();
   private int heavySizeDelta = Agent.getHeavyDelta();
   private int heavySize = Agent.getHeavySize();
   private int stateDelta = Agent.getStateDelta();

   /*
    *    private void JPanel sizePanel()
    *    Contains JComboBox UI components used to select Agent size 
    *    parameters, in pixels.  A filled circle is rendered in a 
    *    demoSizePanel to illustrate the selected size. A JComboBox gets
    *    the user's choice, a JLabel sizeLabel indicates which parameter to choose.
    *    One of each of these is contained in a paramComponentPanel[s]
    *    Each type of component is stored in an array, indexed as follows:
    *    0: start size;		1: minimum size;		2:	lightweight increment from minimum;
    *    3: maximum;	4: heavyweight increment;	5:	state change size ("bite")
    *
    *     Component hierarchy:
    *									sizePanel
    *							 paramComponentPanels
    *				 demoSizePanels		comboxArray		sizeLabels
    *
    */
   private JPanel sizePanel() {

      final int numSizeParams = 6;
      final int startSizeIndex = 0;
      final int minimumSizeIndex = 1;
      final int lightSizeIndex = 2;
      final int maximumSizeIndex = 3;
      final int heavySizeIndex = 4;
      final int stateDeltaIndex = 5;

      /*
       *	an array of JPanels in which to render the size of the chose parameter value
       */
      DemoSizePanel demoSizePanels[] = new DemoSizePanel[numSizeParams];
      for (int i = 0; i < demoSizePanels.length; i++) {
         demoSizePanels[i] = new DemoSizePanel();
      }

      /*	
       *	render intial sizes, except the state delta size (too small)
       */
      demoSizePanels[startSizeIndex].showDemoSize(startSize);
      demoSizePanels[minimumSizeIndex].showDemoSize(minSize);
      demoSizePanels[lightSizeIndex].showDemoSize(lightSize);
      demoSizePanels[maximumSizeIndex].showDemoSize(maxSize);
      demoSizePanels[heavySizeIndex].showDemoSize(heavySize);

      /*
       *	 an array to put each comboBox into
       */
      JComboBox[] comboxArray = new JComboBox[numSizeParams];

      /*
       *	Create comboboxes for each separate setable Agent parameter.
       *	TODO!!!	move low and high constants out of here
       *	 Agent start size, range from 15 to 50
       */
      int lowStartSize = 15;
      int hiStartSize = 50;
      int defaultStartValue = startSize;
      ComboParams comboParams = getComboParams(lowStartSize, hiStartSize, defaultStartValue);

      JComboBox<String> startSizeCombx = new JComboBox<>(comboParams.comboString);
      startSizeCombx.setSelectedIndex(comboParams.defaultIndex);
//		cmbx.setPreferredSize(new Dimension(100, 30));
      startSizeCombx.setFont(usefulFont);

      startSizeCombx.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            JComboBox cmb = (JComboBox) e.getSource();
            String choice = (String) cmb.getSelectedItem();
            startSize = Integer.parseInt(choice);
            demoSizePanels[startSizeIndex].showDemoSize(startSize);

            /*
             *	check to see if chosen start size is compatible with other parameters
             */
            if (startSize <= minSize) {
               String msg = "Start size " + startSize
                  + " must be greater than minimum size " + minSize;
               ErrorDialog.showErrorDialog(dialog, msg);
               paramErrorFlag = true;
            } else if (startSize <= lightSize) {
               String msg = "Start size " + startSize
                  + " must be greater than lightweight size " + lightSize;
               ErrorDialog.showErrorDialog(dialog, msg);
               paramErrorFlag = true;
            } else if (startSize >= heavySize) {
               String msg = "Start size " + startSize
                  + " must be greater than heavyweight size " + heavySize;
               ErrorDialog.showErrorDialog(dialog, msg);
               paramErrorFlag = true;
            } else if (startSize >= maxSize) {
               String msg = "Starting size " + startSize
                  + " must be less than starting size " + maxSize;
               ErrorDialog.showErrorDialog(dialog, msg);
               paramErrorFlag = true;
            } else {
               paramErrorFlag = false;
            }
         }
      });
      comboxArray[startSizeIndex] = startSizeCombx;

      /*
       *	Minimum Agent size, range from 15 to 30
       */
      int lowMinRange = 15;
      int highMinRange = 30;
      int defaultMinValue = minSize;
      comboParams = getComboParams(lowMinRange, highMinRange, defaultMinValue);

      JComboBox<String> minSizeCombx = new JComboBox<>(comboParams.comboString);
      minSizeCombx.setSelectedIndex(comboParams.defaultIndex);
//		cmbx.setPreferredSize(new Dimension(100, 30));
      minSizeCombx.setFont(usefulFont);

      minSizeCombx.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {

            JComboBox cmb = (JComboBox) e.getSource();
            String choice = (String) cmb.getSelectedItem();
            minSize = Integer.parseInt(choice);
            lightSize = minSize + lightSizeDelta;

            /*
             *	Update lightweight and minimum size rendering
             */
            demoSizePanels[minimumSizeIndex].showDemoSize(minSize);
            demoSizePanels[lightSizeIndex].showDemoSize(lightSize);
            /*
             *	check for compatibility with other parameters
             */
            if (minSize >= startSize) {
               String msg = "Minimum size " + minSize
                  + " must be less than starting size " + startSize;
               ErrorDialog.showErrorDialog(dialog, msg);
               paramErrorFlag = true;
            } else if (lightSize >= startSize) {
               String msg = "Light size " + lightSize
                  + " must be less than starting size " + startSize;
               ErrorDialog.showErrorDialog(dialog, msg);
               paramErrorFlag = true;
            } else {
               paramErrorFlag = false;
            }
         }
      });
      comboxArray[minimumSizeIndex] = minSizeCombx;

      /*
       *	Lightweight increment, larger than minimum size
       */
      int lowLightRange = 0;
      int highLightRange = 5;
      int defaultLightValue = lightSizeDelta;
      comboParams = getComboParams(lowLightRange, highLightRange, defaultLightValue);

      JComboBox<String> lightWeightCombx = new JComboBox<>(comboParams.comboString);
      lightWeightCombx.setFont(usefulFont);
      lightWeightCombx.setSelectedIndex(comboParams.defaultIndex);
//		cmbx.setPreferredSize(new Dimension(100, 30));

      lightWeightCombx.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            JComboBox cmb = (JComboBox) e.getSource();
            String choice = (String) cmb.getSelectedItem();
            lightSizeDelta = Integer.parseInt(choice);
            lightSize = minSize + lightSizeDelta;
            demoSizePanels[lightSizeIndex].showDemoSize(lightSize);
            /*
             *	check for compatibility with other parameters
             */
            if (lightSize >= startSize) {
               String msg = "Light size " + lightSize
                  + " must be less than starting size " + startSize;
               ErrorDialog.showErrorDialog(dialog, msg);
               paramErrorFlag = true;
            } else {
               paramErrorFlag = false;
            }
         }
      });
      comboxArray[lightSizeIndex] = lightWeightCombx;

      /*
       *	Agent maximum size
       */
      int lowMaxRange = 25;
      int highMaxRange = 60;
      int defaultMaxValue = maxSize;
      comboParams = getComboParams(lowMaxRange, highMaxRange, defaultMaxValue);

      JComboBox<String> maxSizeCombx = new JComboBox<>(comboParams.comboString);
      maxSizeCombx.setSelectedIndex(comboParams.defaultIndex);
//		cmbx.setPreferredSize(new Dimension(100, 30));
      maxSizeCombx.setFont(usefulFont);

      maxSizeCombx.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            JComboBox cmb = (JComboBox) e.getSource();
            String choice = (String) cmb.getSelectedItem();
            maxSize = Integer.parseInt(choice);
            heavySize = maxSize - heavySizeDelta;

            /*
             *	Update heavyweight and maximum size rendering
             */
            demoSizePanels[maximumSizeIndex].showDemoSize(maxSize);
            demoSizePanels[heavySizeIndex].showDemoSize(heavySize);
            /*
             *	check for compatibility with other parameters
             */
            if (maxSize <= startSize) {
               String msg = "Maximum size " + maxSize
                  + " must be greater than starting size " + startSize;
               ErrorDialog.showErrorDialog(dialog, msg);
               paramErrorFlag = true;
            } else if (heavySize <= startSize) {
               String msg = "Heavyweight size " + heavySize
                  + " must be greater than starting size " + startSize;
               ErrorDialog.showErrorDialog(dialog, msg);
               paramErrorFlag = true;
            } else {
               paramErrorFlag = false;
            }
         }
      });
      comboxArray[maximumSizeIndex] = maxSizeCombx;

      /*
       *	Heavyweight increment from maximum size
       */
      int lowHeavyRange = 0;
      int highHeavyRange = 5;
      int defaultHeavyValue = heavySizeDelta;
      comboParams = getComboParams(lowHeavyRange, highHeavyRange, defaultHeavyValue);

      JComboBox<String> heavyWeightCombx = new JComboBox<>(comboParams.comboString);
      heavyWeightCombx.setSelectedIndex(comboParams.defaultIndex);
//		cmbx.setPreferredSize(new Dimension(100, 30));
      heavyWeightCombx.setFont(usefulFont);

      heavyWeightCombx.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {

            JComboBox cmb = (JComboBox) e.getSource();
            String choice = (String) cmb.getSelectedItem();

            heavySizeDelta = Integer.parseInt(choice);
            heavySize = maxSize - heavySizeDelta;
            /*
             *	Update lightweight and minimum size rendering
             */
            demoSizePanels[heavySizeIndex].showDemoSize(heavySize);
            /*
             *	check for compatibility with other parameters
             */
            if (heavySize <= startSize) {
               String msg = "Heavy size " + heavySize
                  + " must be greater than starting size " + startSize;
               ErrorDialog.showErrorDialog(dialog, msg);
               paramErrorFlag = true;
            } else {
               paramErrorFlag = false;
            }
         }
      });
      comboxArray[heavySizeIndex] = heavyWeightCombx;

      /*
       *	state change size
       */
      int lowStateRange = 0;
      int highStateRange = 5;
      int defaultStateValue = stateDelta;
      comboParams = getComboParams(lowStateRange, highStateRange, defaultStateValue);

      JComboBox<String> stateDeltaCombx = new JComboBox<>(comboParams.comboString);
      stateDeltaCombx.setSelectedIndex(comboParams.defaultIndex);
      stateDeltaCombx.setFont(usefulFont);

      stateDeltaCombx.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {

            JComboBox cmb = (JComboBox) e.getSource();
            String choice = (String) cmb.getSelectedItem();

            stateDelta = Integer.parseInt(choice);
            /*
             *	Update lightweight and minimum size rendering
             */
            demoSizePanels[stateDeltaIndex].showDemoSize(stateDelta);
         }
      });
      comboxArray[stateDeltaIndex] = stateDeltaCombx;

      /*
       *	JLabel for each size parameter
       */
      String[] labelStrings = {"Start", "Minimum", "Lightweight Increment",
         "Maximum", "Heavyweight Increment", "State Change Size"};

      JLabel[] sizeLabels = new JLabel[numSizeParams];

      for (int i = 0; i < sizeLabels.length; i++) {
         sizeLabels[i] = new JLabel();
         sizeLabels[i].setOpaque(true);
         sizeLabels[i].setFont(usefulFont);
         sizeLabels[i].setText(labelStrings[i]);
      }

      /*
       *	An array of JPanels, each one contains: 1. The demoSizePanel, 2. the comboBox, 3. the JLabel
       *	There is no demoSizePanel for state delta size 
       */
      JPanel[] paramComponentPanels = new JPanel[numSizeParams];
      Border usefulBorder = BorderFactory.createLineBorder(Color.darkGray, 2);

      for (int i = 0; i < numSizeParams; i++) {
         paramComponentPanels[i] = new JPanel();
         paramComponentPanels[i].setBorder(usefulBorder);

         /*
          *     add 1. demoSizePanel, 2. comboBox, 3. sizeLabel to the
          *     paramComponentPanels.  There is no demoSizeBox for 
          *     the last paramComponentPanel (state delta size choice)
          *     AKWARD, NEEDS REDESIGN
          */
         if (i != numSizeParams - 1) {
            paramComponentPanels[i].add(demoSizePanels[i]);
         }
         paramComponentPanels[i].add(comboxArray[i]);
         paramComponentPanels[i].add(sizeLabels[i]);

         paramComponentPanels[i].validate();
      }

      /*
       *	Top JPanel to contain each of the individual paramComponentPanels, top to bottom
       */
      JPanel sizePanel = new JPanel();
      sizePanel.setLayout(new BoxLayout(sizePanel, BoxLayout.PAGE_AXIS));

      for (JPanel lbl : paramComponentPanels) {
         sizePanel.add(lbl, LEFT_ALIGNMENT);
      }

      sizePanel.validate();
      return sizePanel;
   }

   /*
    *	a float version of the ComboParams class.   Holds the 
    *     parameters for a JComboBox (comboString, defaultIndex)
    */
   static class ComboParamsFloat {

      String[] comboString;
      int defaultIndex;
   }

   /*
    *	A method to generate the JComboBox parameters (String[] comboString, int defaultIndex).
    *	for a list of floats.  Takes  float[], int default value.
    *	Compose an array of strings of float values.  sets the default 
    *	array index to correspond to the default value.
    */
   private static ComboParamsFloat getFloatComboParams(float[] floatChoices, float defaultValue) {

      String[] combxString = new String[floatChoices.length];
      int defaultIndex = 1;

      for (int i = 0; i < floatChoices.length; i++) {
         if (defaultValue == floatChoices[i]) {
            defaultIndex = i;
         }
         combxString[i] = Float.toString(floatChoices[i]);
      }

      ComboParamsFloat comboParams = new ComboParamsFloat();
      comboParams.comboString = combxString;
      comboParams.defaultIndex = defaultIndex;
      return comboParams;
   }

   private float minSpeed = Agent.getMinSpeed();
   private float maxSpeed = Agent.getMaxSpeed();

   private float lightSpeed = Agent.getLightSpeedFactor();
   private float heavySpeed = Agent.getHeavySpeedFactor();

   private final float[] minSpeedRange = {0.1f, 0.2f, 0.3f, 0.4f, 0.5f};
   private final float[] maxSpeedRange = {2.5f, 2.6f, 2.7f, 2.8f, 2.9f, 3.0f, 3.1f, 3.2f, 3.3f, 3.4f, 3.5f};
   private final float[] lightSpeedRange = {1.0f, 1.1f, 1.2f, 1.3f, 1.4f, 1.5f, 1.6f, 1.7f, 1.8f, 1.9f, 2.0f, 2.1f};
   private final float[] heavySpeedRange = {0.50f, 0.55f, 0.60f, 0.65f, 0.70f, 0.75f, 0.80f, 0.85f, 0.90f, 0.95f, 1.0f};

   /*
    *     A JPanel to hold GUI components to set Agent speed parameters.  
    *     minimum speed, maximum speed, lightweight speed factor, heavyweight
    *     speed factor.  Similar design as sizePanel, see description.
    *     Component hierarchy:
    *									speedPanel
    *							 paramComponentPanels
    *				 		comboxArray		speedLabels
    */
   private JPanel speedPanel() {

      /*
       *     See sizePanel design.  
       *	number of speed parameters to set
       *	high, low, delta light, delta heavy
       */
      int numSpeedParams = 4;

      /*
       *	 an array to put each comboBox into
       */
      JComboBox[] comboxArray = new JComboBox[numSpeedParams];

      /*
       *	Mimimum speed
       */
      ComboParamsFloat comboParams = getFloatComboParams(minSpeedRange, minSpeed);
      JComboBox<String> minSpeedCombx = new JComboBox<>(comboParams.comboString);
      minSpeedCombx.setSelectedIndex(comboParams.defaultIndex);
      minSpeedCombx.setFont(usefulFont);

      minSpeedCombx.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {

            JComboBox cmb = (JComboBox) e.getSource();
            String choice = (String) cmb.getSelectedItem();
            minSpeed = Float.parseFloat(choice);
         }
      });
      comboxArray[0] = minSpeedCombx;

      /*
       *	Maximum speed
       */
      comboParams = getFloatComboParams(maxSpeedRange, maxSpeed);
      JComboBox<String> maxSpeedCombx = new JComboBox<>(comboParams.comboString);
      maxSpeedCombx.setSelectedIndex(comboParams.defaultIndex);
      maxSpeedCombx.setFont(usefulFont);

      maxSpeedCombx.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {

            JComboBox cmb = (JComboBox) e.getSource();
            String choice = (String) cmb.getSelectedItem();
            maxSpeed = Float.parseFloat(choice);
         }
      });
      comboxArray[1] = maxSpeedCombx;

      /*
       *	Light speed acceleration factor
       */
      comboParams = getFloatComboParams(lightSpeedRange, lightSpeed);
      JComboBox<String> lightSpeedCombx = new JComboBox<>(comboParams.comboString);
      lightSpeedCombx.setSelectedIndex(comboParams.defaultIndex);
      lightSpeedCombx.setFont(usefulFont);

      lightSpeedCombx.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            JComboBox cmb = (JComboBox) e.getSource();
            String choice = (String) cmb.getSelectedItem();
            lightSpeed = Float.parseFloat(choice);
         }
      });
      comboxArray[2] = lightSpeedCombx;

      /*
       *	 heavy speed deceleration factor
       */
      comboParams = getFloatComboParams(heavySpeedRange, heavySpeed);
      JComboBox<String> heavySpeedCombx = new JComboBox<>(comboParams.comboString);
      heavySpeedCombx.setSelectedIndex(comboParams.defaultIndex);
      heavySpeedCombx.setFont(usefulFont);

      heavySpeedCombx.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            JComboBox cmb = (JComboBox) e.getSource();
            String choice = (String) cmb.getSelectedItem();
            heavySpeed = Float.parseFloat(choice);
         }
      });
      comboxArray[3] = heavySpeedCombx;

      /*
       *	JLabel for each parameter
       */
      String[] labelStrings = {"Minimum Speed", "Maximum Speed", "Lightweight Factor",
         "Heavyweight Factor"};

      JLabel[] sizeLabels = new JLabel[numSpeedParams];

      for (int i = 0; i < sizeLabels.length; i++) {
         sizeLabels[i] = new JLabel();
         sizeLabels[i].setOpaque(true);
         sizeLabels[i].setFont(usefulFont);
         sizeLabels[i].setText(labelStrings[i]);
      }

      /*
       *	An array of JPanels, each one contains a comboBox and the corresponding JLabel
       */
      JPanel[] paramComponentPanels = new JPanel[numSpeedParams];
      Border usefulBorder = BorderFactory.createLineBorder(Color.darkGray, 2);

      for (int i = 0; i < numSpeedParams; i++) {
         paramComponentPanels[i] = new JPanel();
         paramComponentPanels[i].setBorder(usefulBorder);

         paramComponentPanels[i].add(comboxArray[i]);
         paramComponentPanels[i].add(sizeLabels[i]);

         paramComponentPanels[i].validate();
      }

      /*
       *	Top JPanel to contain each of the individual paramComponentPanels, top to bottom
       */
      JPanel speedPanel = new JPanel();
      speedPanel.setLayout(new BoxLayout(speedPanel, BoxLayout.PAGE_AXIS));

      for (JPanel lbl : paramComponentPanels) {
         speedPanel.add(lbl, LEFT_ALIGNMENT);
      }

      speedPanel.validate();
      return speedPanel;

   }

   private void setAgentParamters() {

      Agent.setStartSize(startSize);
      Agent.setMinSize(minSize);
      Agent.setLightDelta(lightSizeDelta);
      Agent.setLightSize(lightSize);
      Agent.setMaxSize(maxSize);
      Agent.setHeavyDelta(heavySizeDelta);
      Agent.setHeavySize(heavySize);
      Agent.setStateDelta(stateDelta);

      Agent.setMinSpeed(minSpeed);
      Agent.setMaxSpeed(maxSpeed);
      Agent.setLightSpeedFactor(lightSpeed);
      Agent.setHeavySpeedFactor(heavySpeed);
   }

   /*
    *    A JPanel in which to render a circle of the selected value
    *     to provide a visual cue for the parameter size selected.
    */
   private static class DemoSizePanel extends JPanel {

      int demoSize;

      Dimension panelSize = new Dimension(70, 70);  // hardcode to maximum Agent size of 60 pixels
      int xLoc;
      int yLoc;
      private final static Border usefulBorder = BorderFactory.createLineBorder(Color.darkGray, 2);

      DemoSizePanel() {
         setPreferredSize(panelSize);
         setBorder(usefulBorder);
      }

      public void showDemoSize(int ds) {
         demoSize = ds;
         xLoc = panelSize.width / 2 - demoSize / 2;
         yLoc = panelSize.width / 2 - demoSize / 2;
         repaint();
      }

      @Override
      public void paintComponent(Graphics g) {
         super.paintComponent(g);
         Graphics2D g2 = (Graphics2D) g;
         g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);
         g2.setRenderingHint(RenderingHints.KEY_RENDERING,
            RenderingHints.VALUE_RENDER_SPEED);

         g2.setColor(Color.blue);	// a different color?  

         Ellipse2D.Float demoSizeCircle = new Ellipse2D.Float(xLoc, yLoc, demoSize, demoSize);
         g2.fill(demoSizeCircle);

      }
   }

}
