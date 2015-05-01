/*
 *    Copyright 2014 -15 Joseph Harrington, all rights reserved.  No
 *     portion of this source may be used with express permission.
 *  
 *    Rev 1.2 March 2015. 
 *
 *    Complete redesign of DashBoard Class.  Contains barGraphs to 
 *     display Agent size, speed distributions, average size, speed, 
 *     Agent Count.  Bar graphs are updated via a scheduled thread
 *     executor service.
 *     Abstract all  application-specific code out of BarGraph class.
 *
 *	TODO:	numberBins is fixed at 15, add user setting.
 */
package changeagent;

import java.awt.*;
import java.awt.Color;
import java.awt.event.*;
import java.awt.geom.*;
//import java.awt.event.MouseAdapter;
//import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.swing.*;
import javax.swing.BorderFactory;
import javax.swing.border.Border;
import java.lang.reflect.Array;
import static changeagent.AgentConstants.*;
import static javax.swing.SwingConstants.CENTER;
import static javax.swing.SwingConstants.LEFT;

public class Dashboard2 {

   private static Dashboard2 db;
   private final int numberBins = 15;
   private final int countBars = 15;

   private final BarGraph sizeGraphA;
   private final BarGraph sizeGraphB;
   private final BarGraph speedGraphA;
   private final BarGraph speedGraphB;
   private final BarGraph averageSizeA;
   private final BarGraph averageSizeB;
   private final BarGraph averageSpeedA;
   private final BarGraph averageSpeedB;
   private final BarGraph countGraphA;
   private final BarGraph countGraphB;

   /*
    *	Dashboard layout dimensions
    */
   private final int dashWidth = 1000;
   private final int dashHeight = 600;
   private final int graphGap = 10;
   private final Point anchorPoint = new Point(20, 70);
   private final Dimension multiGraphDim = new Dimension(280, 200);    // mulitple bars for array of values
   private final Dimension singleGraphDim = new Dimension(35, 200);    // single bar for averayes;

   private final JFrame f;

   Dashboard2() {

      /*
       *     Dashboard refresh rate, in millis 
       */
      final long refreshRate = 1000;

      int x;
      f = new JFrame("ChangeAgent Dashboard 1.2 Beta");
      f.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

      f.setSize(dashWidth, dashHeight);
      f.setResizable(false);
      f.getContentPane().setBackground(Color.white);
      f.setLayout(null);

      /*
       *	min and max speed in Agent class are x-coord and y-coord increments.  
       *	they are displayed n dashboard as absolute values with Mr. Pythagoras'
       *	help
       */
      float temp = minSpeed;
      minSpeed = (float) Math.hypot(temp, temp);
      temp = maxSpeed;
      maxSpeed = (float) Math.hypot(temp, temp);

      /*
       *	initialize the size and speed graph bin values, based on max and min values
       */
      initializeBinFilters(numberBins);

      /* 
       *     layout the graphs in dashboard, arranged in grid as follows:
       *
       *	1: group A size	2: A average size	3:group A speed		4: average speed	5: count
       *	6: group B size	7: B average size	8:group B speed	9: average speed	10: count
       *
       *	The X, Y location of each is calculated parametrically based on the location of graph 1, 
       *	the width of graph gaps and the graph dimensions.  
       */
      Point graphLocation = new Point(anchorPoint.x, anchorPoint.y);

      /*	
       *		Group A size graph
       *		X	-	-	-	-
       *		-	-	-	-	-
       */
      sizeGraphA = new BarGraph(graphLocation, multiGraphDim, numberBins);
      sizeGraphA.setBarGraphColor(AColor);
      sizeGraphA.setBarScaleFactor(3);
      sizeGraphA.setTopLabel("Group A Size");
      String sizeLabelText = String.format("Size %d to %d", minSize, maxSize);
      sizeGraphA.setBottomLabel(sizeLabelText);

      /*
       *	graph 2, group A average size
       *		-	X	-	-
       *		-	-	-	-
       */
      graphLocation.x = anchorPoint.x + graphGap + multiGraphDim.width;
      graphLocation.y = anchorPoint.y;
      averageSizeA = new BarGraph(graphLocation, singleGraphDim, 1);
      averageSizeA.setBarGraphColor(AColor);
      averageSizeA.setBarScaleFactor(1);
      averageSizeA.setTopLabel("Avg");
      String averageSizeString = String.format("%d", maxSize);
      averageSizeA.setBottomLabel(averageSizeString);

      /*
       *		graph 3, group A speed
       *		-	-	X	-	-
       *		-	-	-	-	-
       */
      graphLocation.x = anchorPoint.x + multiGraphDim.width + singleGraphDim.width + 2 * graphGap;
      graphLocation.y = anchorPoint.y;
      speedGraphA = new BarGraph(graphLocation, multiGraphDim, numberBins);
      speedGraphA.setBarGraphColor(AColor);
      speedGraphA.setBarScaleFactor(3);
      speedGraphA.setTopLabel("Group A Speed");
      String speedLabelText = String.format("Speed %1$.2f to %2$.2f", minSpeed, maxSpeed);
      speedGraphA.setBottomLabel(speedLabelText);

      /*
       *		graph 4, group A average speed
       *		-	-	-	X	-
       *		-	-	-	-	-
       */
      graphLocation.x = anchorPoint.x + 2 * multiGraphDim.width + singleGraphDim.width + 3 * graphGap;
      graphLocation.y = anchorPoint.y;
      averageSpeedA = new BarGraph(graphLocation, singleGraphDim, 1);
      averageSpeedA.setBarGraphColor(AColor);
      averageSpeedA.setBarScaleFactor(1);
      String averageSpeedString = String.format("%.2f", maxSpeed);
      averageSpeedA.setTopLabel("Avg");
      averageSpeedA.setBottomLabel(averageSpeedString);

      /*
       *	graph 5, count
       *		-	-	-	-	X
       *		-	-	-	-	-
       */
      graphLocation.x += singleGraphDim.width + graphGap;
      countGraphA = new BarGraph(new Point(graphLocation.x, graphLocation.y), multiGraphDim, countBars);
      countGraphA.setBarGraphColor(AColor);
      countGraphA.setBarScaleFactor(1);
      countGraphA.setTopLabel("Group A Count");
      String countLabelText = String.format("%d Total Agents", agentCount);
      countGraphA.setBottomLabel(countLabelText);

      /*
       *	graph 6, group B size
       *		-	-	-	-	-
       *		X	-	-	-	-
       */
      graphLocation.x = anchorPoint.x;
      graphLocation.y = anchorPoint.y + multiGraphDim.height + graphGap;
      sizeGraphB = new BarGraph(graphLocation, multiGraphDim, numberBins);
      sizeGraphB.setBarGraphColor(BColor);
      sizeGraphB.setBarScaleFactor(3);
      sizeGraphB.setTopLabel("Group B Size");
      sizeGraphB.setBottomLabel(sizeLabelText);

      /*
       *	graph 7, group B average size
       *		-	-	-	-	-
       *		-	X	-	-	-
       */
      graphLocation.x = anchorPoint.x + graphGap + multiGraphDim.width;
      graphLocation.y = anchorPoint.y + multiGraphDim.height + graphGap;
      averageSizeB = new BarGraph(graphLocation, singleGraphDim, 1);
      averageSizeB.setBarScaleFactor(1);
      averageSizeB.setBarGraphColor(BColor);
      averageSizeB.setTopLabel("Avg");
      averageSizeB.setBottomLabel(averageSizeString);

      /*
       *	graph 8, group B speed
       *		-	-	-	-	-
       *		-	-	X	-	-
       */
      graphLocation.x = anchorPoint.x + multiGraphDim.width + singleGraphDim.width + 2 * graphGap;
      graphLocation.y = anchorPoint.y + multiGraphDim.height + graphGap;
      speedGraphB = new BarGraph(graphLocation, multiGraphDim, numberBins);
      speedGraphB.setBarGraphColor(BColor);
      speedGraphB.setBarScaleFactor(3);
      speedGraphB.setTopLabel("Group B Speed");
      speedGraphB.setBottomLabel(speedLabelText);

      /*
       *	graph 9, group B average speed
       *		-	-	-	-	-
       *		-	-	-	X	-
       */
      graphLocation.x = anchorPoint.x + 2 * multiGraphDim.width + singleGraphDim.width + 3 * graphGap;
      graphLocation.y = anchorPoint.y + multiGraphDim.height + graphGap;
      averageSpeedB = new BarGraph(graphLocation, singleGraphDim, 1);
      averageSpeedB.setBarGraphColor(BColor);
      averageSpeedB.setBarScaleFactor(1);
      averageSpeedB.setTopLabel("Avg");
      averageSpeedB.setBottomLabel(averageSpeedString);

      /*
       *	graph 10, count
       *		-	-	-	-	-
       *		-	-	-	-	X
       */
      graphLocation.x += singleGraphDim.width + graphGap;
      countGraphB = new BarGraph(new Point(graphLocation.x, graphLocation.y), multiGraphDim, countBars);
      countGraphB.setBarGraphColor(BColor);
      countGraphB.setBarScaleFactor(1);
      countGraphB.setTopLabel("Group B Count");
      countGraphB.setBottomLabel(countLabelText);

      f.add(sizeGraphA);
      f.add(sizeGraphB);
      f.add(speedGraphA);
      f.add(speedGraphB);
      f.add(averageSizeA);
      f.add(averageSizeB);
      f.add(averageSpeedA);
      f.add(averageSpeedB);
      f.add(countGraphA);
      f.add(countGraphB);

      f.setVisible(true);

      /*
       *	get executor, launch thread
       *	TODO:    change to single thread
       */
      dashExecutor = new ScheduledThreadPoolExecutor(4);  //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
      dashExecutor.scheduleAtFixedRate(
         new DashboardThread(), 0L, refreshRate, TimeUnit.MILLISECONDS);

      // used for debugging, leave it 
      f.getContentPane()
         .addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e
            ) {
               if (e.getButton() == 1) {
                  updateDash();
               }
            }
         }
         );

   }

   private final ScheduledThreadPoolExecutor dashExecutor;
   private boolean dashboardUp = true;

   class DashboardThread implements Runnable {

      JFrame frame;

      DashboardThread() {
      }

      @Override
      public void run() {

         if (dashboardUp) {
            try {
               updateDash();
            } catch (Exception e) {
            }
         } else {
            stopThread();
            f.setVisible(false);
         }
      }
   }

   private float[] sizeFilter = {0.0f};
   private float[] speedFilter = {0.0f};
   private int maxSize = Agent.getMaxSize();
   private int minSize = Agent.getMinSize();
   private float maxSpeed = Agent.getMaxSpeed();
   private float minSpeed = Agent.getMinSpeed();
   private int agentCount = Agent.getNumberOfAgents();

   /*
    *    set bin increments between min and max values for size and speed.
    *    TODO:    needs a check for numberBins > 3
    */
   final void initializeBinFilters(int numberBins) {

      sizeFilter = new float[numberBins - 1];
      int sizeLength = Array.getLength(sizeFilter);

      float sizeIncrement = (maxSize - minSize) / (numberBins);	// 
      sizeFilter[0] = minSize + sizeIncrement;
      sizeFilter[sizeLength - 1] = maxSize;

      for (int i = 1; i < numberBins - 1; i++) {
         sizeFilter[i] = sizeFilter[i - 1] + sizeIncrement;
      }

      speedFilter = new float[numberBins - 1];
      int speedLength = Array.getLength(speedFilter);

      float speedIncrement = (maxSpeed - minSpeed) / (numberBins);	// 
      speedFilter[0] = minSpeed + speedIncrement;
      speedFilter[speedLength - 1] = maxSpeed;

      for (int i = 1; i < speedFilter.length - 1; i++) {
         speedFilter[i] = speedFilter[i - 1] + speedIncrement;
      }
   }

//   static void showDashboard() {
//      db = new Dashboard2();
//   }

   private void stopThread() {
      ArrayList<Runnable> runnableList;
      runnableList = (ArrayList<Runnable>) dashExecutor.shutdownNow();
   }

   void shutDown() {
      dashboardUp = false;
   }

   /*
    *     fields and methods to process Agent size, speed data.
    */
   class AgentData {

      private ArrayList<Agent> agentList = Agent.getAgentList();

      private final ArrayList<Float> sizeDataA;
      private final ArrayList<Float> speedDataA;
      private final ArrayList<Float> sizeDataB;
      private final ArrayList<Float> speedDataB;

      private float sizeA;
      private float sizeB;
      private float deltaX_A, deltaY_A;
      private float deltaX_B, deltaY_B;
      private float speedA;
      private float speedB;
      private float sizeASum = 0f;
      private float sizeBSum = 0f;
      private float speedASum = 0f;
      private float speedBSum = 0f;
      private int countA = 0;
      private int countB = 0;

      AgentData() {
         sizeDataA = new ArrayList<>();
         speedDataA = new ArrayList<>();
         sizeDataB = new ArrayList<>();
         speedDataB = new ArrayList<>();
      }

      /*
       *     call Agent getters to get size, speed data
       */
      void readData() {

         for (Agent ag : agentList) {

            int group = ag.getAgentGroup();

            if (group == A_GROUP) {
               sizeA = ag.getDiameter();
               sizeDataA.add(sizeA);
               sizeASum += sizeA;

               deltaX_A = ag.getDeltaX();
               deltaY_A = ag.getDeltaY();

               /*
                *     Agent speed is deltaX and deltaY.  display speed distribution as 
                *     speed in given direction.  This will need to be revised when Agent
                *     is revised to generate random bearing and speed
                */
               double sum = (deltaX_A * deltaX_A) + (deltaY_A * deltaY_A);
               speedA = (float) Math.sqrt(sum);
               speedASum += speedA;

               speedDataA.add(speedA);
               countA++;
            } else if (group == B_GROUP) {
               sizeB = ag.getDiameter();
               sizeDataB.add(sizeB);
               sizeBSum += sizeB;
               deltaX_B = ag.getDeltaX();
               deltaY_B = ag.getDeltaY();
               speedB = (float) Math.hypot(deltaX_B, deltaY_B);  // change to sqrt
               speedBSum += speedB;
               speedDataB.add(speedB);
               countB++;
            }
         }
      }

      ArrayList<Float> getSizeDataA() {
         return sizeDataA;
      }

      ArrayList<Float> getSizeDataB() {
         return sizeDataB;
      }

      ArrayList<Float> getSpeedDataA() {
         return speedDataA;
      }

      ArrayList<Float> getSpeedDataB() {
         return speedDataB;
      }

      float getAverageSizeA() {
         return sizeASum / sizeDataA.size();
      }

      float getAverageSizeB() {
         return sizeBSum / sizeDataB.size();
      }

      float getAverageSpeedA() {
         return speedASum / speedDataA.size();
      }

      float getAverageSpeedB() {
         return speedBSum / speedDataB.size();
      }

      int getACount() {
         return countA;
      }

      int getBCount() {
         return countB;
      }

   }

   /*
    *     needs user interface to set number of bars.
    */
   private int[] countA = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
   private int[] countB = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
   private int updateCounter = 0;

   /*
    *	get data, sort, refresh graphs
    */
   void updateDash() {

      AgentData ad = new AgentData();

      /*
       *	Get the data
       */
      ad.readData();

      /*
       *	sort the data into bins
       */
      int[] sizeSortA = dataSorter(ad.getSizeDataA(), sizeFilter);
      int[] sizeSortB = dataSorter(ad.getSizeDataB(), sizeFilter);
      int[] speedSortA = dataSorter(ad.getSpeedDataA(), speedFilter);
      int[] speedSortB = dataSorter(ad.getSpeedDataB(), speedFilter);

      /*
       *	refresh graphs
       */
      sizeGraphA.refreshGraph(sizeSortA);
      sizeGraphB.refreshGraph(sizeSortB);

      speedGraphA.refreshGraph(speedSortA);
      speedGraphB.refreshGraph(speedSortB);

      averageSizeA.refreshGraph(ad.getAverageSizeA(), maxSize);
      averageSizeB.refreshGraph(ad.getAverageSizeB(), maxSize);

      averageSpeedA.refreshGraph(ad.getAverageSpeedA(), maxSpeed);
      averageSpeedB.refreshGraph(ad.getAverageSpeedB(), maxSpeed);

      /* 
       *	shift historiogram bars every 10 refreshes
       */
      if (updateCounter != 0) {

         countA[countA.length - 1] = ad.getACount();
         countB[countB.length - 1] = ad.getBCount();

         for (int x = 1; x < countA.length; x++) {
            countA[x - 1] = countA[x];
            countB[x - 1] = countB[x];
         }

         int reference = ad.getACount() + ad.getBCount();
         countGraphA.refreshGraph(countA, reference);
         countGraphB.refreshGraph(countB, reference);
      }
      updateCounter++;
   }

   /*
    *	sort list of floats into bins according bin values in filter
    */
   private int[] dataSorter(ArrayList<Float> rawData, float[] filter) {
      ArrayList<Float> rd = rawData;
      int[] distributionReturnArray = new int[filter.length + 1];
      int rdIntValue;
      float value;

      /*
       *	for each data value, sort into the  buckets, representing the range of each value in
       *	filter array
       */
      for (int a = 0; a < rd.size(); a++) {
         value = rd.get(a);
         //	value is less than first filter value 
         if (value < filter[0]) {
            distributionReturnArray[0]++;
         }

         // value is between filter[x] and filter[x+1]
         for (int x = 0; x < filter.length - 1; x++) {
            if (value >= filter[x] && value < filter[x + 1]) {
               distributionReturnArray[x + 1]++;
            }
         }

         // value is greater than maximum filter value, goes into the last bin
         if (value >= filter[filter.length - 1]) {
            distributionReturnArray[filter.length]++;
         }
      }
      return distributionReturnArray;

   }

}

/*
 *	class to create and render bar graphs, with refresh methods to update graph
 */
class BarGraph extends JPanel {

   private Color barColor;
   private int graphWidth;
   private int graphHeight;
   private Font labelFont;
   private int labelHeight;
   private int barHeightScale = 1;

   /*
    *     x locations of each bar, fixed for each graph based on 
    *	JPanel width, gap width and bar width
    */
   private int[] barLocX;
   private int barWidth;
   private int numberOfBars;
   private int gapWidth = 5;		// needs setter
   JLabel topLabel;
   JLabel bottomLabel;
   private ArrayList<Bar> barList = new ArrayList<>();  // used in refresh() and paintComponent();

   /*
    *	constructor parameters are location and size in enclosing swing component,
    *	number of bars.
    */
   public BarGraph(Point location, Dimension size, int Bars) {

      numberOfBars = Bars;
      this.setBounds(location.x, location.y, size.width, size.height);
      this.setOpaque(true);
      this.setBackground(Color.lightGray);
      this.setLayout(null);

      /*
       *	instantiate font, get height
       *	TODO:	add font select to API
       */
      labelFont = new Font("Serif", Font.BOLD, 18);
      FontMetrics fmet = getFontMetrics(labelFont);
      labelHeight = fmet.getHeight();

      topLabel = new JLabel();
      topLabel.setFont(labelFont);
      topLabel.setSize(new Dimension(size.width, labelHeight));
      topLabel.setLocation(0, 0);
      topLabel.setText("");

      bottomLabel = new JLabel();
      bottomLabel.setFont(labelFont);
      bottomLabel.setSize(new Dimension(size.width, labelHeight));
      bottomLabel.setLocation(0, size.height - labelHeight);
      bottomLabel.setText("");

      this.add(topLabel);
      this.add(bottomLabel);

      /*
       *	TO DO change this to set number of bars to filter size
       */
      barLocX = new int[Bars];

      graphWidth = size.width;
      graphHeight = size.height - (2 * labelHeight);

      /*
       *    set the x values of the bars once, they will be fixed for each graph
       */
      setBarXLocations();
   }

   void setBarGraphColor(Color color) {
      barColor = color;
   }

   void setBarScaleFactor(int factor) {
      barHeightScale = factor;
   }

   void setTopLabel(String msg) {
      topLabel.setText(msg);
   }

   void setBottomLabel(String msg) {
      bottomLabel.setText(msg);
   }

   /*
    *  calculate the x position of each bar rectangle, these values are 
    *	for a given width and number of bars.
    */
   private void setBarXLocations() {

      int totalGapWidth = gapWidth * (numberOfBars + 1);
      barWidth = (graphWidth - totalGapWidth) / (numberOfBars);
      for (int j = 0; j < numberOfBars; j++) {
         barLocX[j] = (gapWidth * (j + 1)) + (barWidth * j);
      }
   }

   /*
    *	this method is used for histogram graphs, each bar represents
    *	the height of the number in each bin
    */
   public void refreshGraph(int[] bins) {
      barList = getBarList(bins);
      repaint();
   }

   /*
    *	returns list of bars, the height of each bar is the number
    *	of items in a given bin divided by sum of all the bins,
    *	dataBins[x]  /  (totalCount)
    */
   private ArrayList<Bar> getBarList(int[] dataBins) {

      /*
       *    get the largest bin value  and the total of all bins
       *	!!! TODO: implement option for show graph normalized to largest value or absolute value
       */
      int totalCount = 0;
      int largestBinValue = 0;
      for (int x = 0; x < dataBins.length; x++) {
         totalCount = totalCount + dataBins[x];
         if (dataBins[x] > largestBinValue) {
            largestBinValue = dataBins[x];
         }
      }

      //leave a small gap between the top of a maximum value bar and the top of the graph)
      int maxHeight = graphHeight;

      int y, barHeight;
      int numberBins = dataBins.length;
      ArrayList<Bar> returnBarList = new ArrayList<>();

      /*
       *   for each value, create a new Bar
       */
      for (int j = 0; j < numberBins; j++) {
         float barHeightRatio = (float) dataBins[j] / (float) totalCount;
         barHeight = (int) (barHeightRatio * graphHeight * barHeightScale);
         y = graphHeight - barHeight + labelHeight;

         /*
          *	if bar height exceeds the graph drawing area, cap y location
          *	at top of graph rendering area, barHeight at graphHeight
          */
         if (y < labelHeight) {
            y = labelHeight;
            barHeight = graphHeight;
         }

         returnBarList.add(new Bar(new Point(barLocX[j], y), new Dimension(barWidth, barHeight)));

      }
      return returnBarList;
   }


   /*
    *	histogram bar graph, height of bar is number in a bin divided
    *	by a reference value
    */
   public void refreshGraph(int[] graphData, int reference) {
      barList = getBarList(graphData, reference);
      repaint();
   }

   /*
    *	returns list of bars where height is bin value divided by a reference
    */
   private ArrayList<Bar> getBarList(int[] dataBins, int reference) {

      int y, barHeight;
      int numberBins = dataBins.length;
      ArrayList<Bar> returnBarList = new ArrayList<>();

      for (int j = 0; j < numberBins; j++) {
         float barHeightRatio = (float) dataBins[j] / (float) reference;
         barHeight = (int) (barHeightRatio * graphHeight * barHeightScale);
         y = graphHeight - barHeight + labelHeight;
         returnBarList.add(new Bar(new Point(barLocX[j], y), new Dimension(barWidth, barHeight)));
      }
      return (returnBarList);
   }

   /*
    *	sinlge bar graph, height is value / max.
    *	for this graph, calculate the bar height in this method rather
    *	than calling getBarList()
    */
   public void refreshGraph(float value, float max) {

      float barHeightRatio = value / max;
      int barHeight = (int) (barHeightRatio * graphHeight * barHeightScale);
      int y = graphHeight - barHeight + labelHeight;
      barList.add(new Bar(new Point2D.Float(barLocX[0], y), barWidth, barHeight));

      repaint();
   }

   /*
    * inner class Bar represents each bar of the bargraph.  Bar extends Rectangle2D,
    * constructor parameters are location and size
    */
   private class Bar extends Rectangle2D.Float {

      private Bar(Point barLoc, Dimension barSize) {
         super.x = barLoc.x;
         super.y = barLoc.y;
         super.width = barSize.width;
         super.height = barSize.height;
      }

      private Bar(Point2D.Float barLoc, float width, float height) {
         super.x = barLoc.x;
         super.y = barLoc.y;
         super.width = width;
         super.height = height;
      }
   }

   @Override
   public void paintComponent(Graphics g) {

      super.paintComponent(g);

      if (barList != null) {
         paintGraph(g);
      }
   }

   /*
    * draw the bars
    */
   int paintCount = 1;

   private void paintGraph(Graphics g) {

      Graphics2D g2 = (Graphics2D) g;
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
         RenderingHints.VALUE_ANTIALIAS_ON);

      for (int j = 0; j < barList.size(); j++) {
         g2.setPaint(barColor);
         g2.fill(barList.get(j));
      }
   }
}	/* close class BarGraph */
