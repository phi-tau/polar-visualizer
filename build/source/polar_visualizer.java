import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.text.DecimalFormat; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class polar_visualizer extends PApplet {

/* Polar Visualizer
 * By: Philip Taylor, Trinity Valley School
 * Visualize a polar graph with Processing style (CW-> +) angles. User configuralble options are immediately below this block.
 */

//User configurable
//-------------------------------
//drawing options
boolean processingStyle=false;    //Processing style angles (CW)?
boolean waitForStep=false;
boolean drawVectors=false; //draw lines from origin to point
double animationWidth=8*PI; //stop at integer multiples of this number
final double DELTATHETA = PI/4000;   //what change in angle? needs to be multiple of PI. smaller->slower animation
int sizeOfPoint = 4;  //strokeWeight of the highlited point. Use 2 for same as all other point
boolean saveImages=true;
boolean useDegreesForLabels=true;

//options about relation
double theta1=0; //Beginning theta in domain
double theta2=8*PI; //Ending theta in domain
String r = "r=100(e^(sin(t))-2*cos(4t)+sin^5((2t-pi)/24))"; //String representation of r(\u03b8)
public double r(double theta) {
  return 100*(exp((float) Math.sin(theta))-2*Math.cos(4*theta)+pow((float) Math.sin((2*theta-PI)/24),5)); //a function of theta
}

//-------------------------------


//Do not change items beneath this line
//-------------------------------

DecimalFormat twoDec;
DecimalFormat noDec;
double t;
final int WELCOME=0;
final int ANIMATE=2;
final int ANIMATE2=1;
final int FINISHED=3;
final int WAITING=4;
int state;
boolean doAnimate2=false;
boolean wait=false;

double animatet1;
double animatet2;


float infopanelx; //top left corner of where info panel starts
float graphwidth; //width of graph


//Setup the window
public void setup() {
  

  twoDec = new DecimalFormat("0.00");
  noDec = new DecimalFormat("0");
  state=WELCOME;
}


public void draw() {
  if (wait) { //wait for eyboard or mouse
    state=WAITING;
  }
  switch(state) {
  case WELCOME:
    //graph background
    textSize(12);
    infopanelx=.75f*width;
    graphwidth = infopanelx;
    twoDec = new DecimalFormat("0.00");
    noDec = new DecimalFormat("0");
    animatet1=theta1;
    animatet2=theta2;
    t=theta1;
    background(255);
    strokeWeight(1);
    stroke(230);
    line(graphwidth/2, 0, graphwidth/2, height);
    line(0, height/2, graphwidth, height/2);

    for (int i=0; i<2*graphwidth; i+=100) {
      fill(230);
      text(i/2, graphwidth/2+i/2, height/2);
      text(i/2, graphwidth/2-i/2, height/2);
      text(i/2, graphwidth/2, height/2+i/2);
      text(i/2, graphwidth/2, height/2-i/2);
      noFill();
      ellipse(graphwidth/2, height/2, i, i);
    }

    drawInfoPanel(); //draw it
    state=ANIMATE2;
    break;
  case ANIMATE: //graph a light copy
    if (t>=animatet2) {
      state=ANIMATE2;
    } else if (t>=theta2) {
      if(saveImages){
      PImage graph = get(0,0,(int) graphwidth,height);
      graph.save("graph"+r+".png");
      state=WELCOME;
    }
    }
    translate(graphwidth/2, height/2);
    stroke(255, 0, 0);
    strokeWeight(2);
    if(processingStyle){
      point(new Float(r(t)*Math.cos(t)), new Float(r(t)*Math.sin(t)));
    }else{
      point(new Float(r(t)*Math.cos(t)), new Float(-r(t)*Math.sin(t)));
    }
    t+=DELTATHETA;



    break;
  case WAITING:
    break;
  case ANIMATE2:  //This is where we update point label and wait to continue

    drawLabel();
    animatet2=animatet1+animationWidth;
    animatet1=animatet2;
    if(waitForStep){
    wait=true;
  }
    state=ANIMATE;
    break;
  }
}

//Draw label for point
public void drawLabel() {

  stroke(255, 0, 0);
  strokeWeight(1);
  rect(infopanelx+15, 105, 140, 25);
  fill(0);

  String angleLabel;
  if(useDegreesForLabels){
    angleLabel=convertRadiansToDegrees(animatet1)+"\u00b0";
  }else{
    angleLabel=convertDecimalToMultipleOfPi(animatet1);
  }
  text("("+ noDec.format(r(animatet1))+","+angleLabel+ ")", infopanelx+15, 125);

  pushMatrix();
  translate(graphwidth/2, height/2);
  stroke(0, 0, 255);
  strokeWeight(sizeOfPoint);
  if(processingStyle){
  point(new Float(r(animatet2)*Math.cos(animatet2)), new Float( r(animatet2)*Math.sin(animatet2)));
}else{
  point(new Float(r(animatet2)*Math.cos(animatet2)), new Float(- r(animatet2)*Math.sin(animatet2)));

}

  //draw line from origin
  if (drawVectors) {
    stroke(100);
    strokeWeight(1);
    if(processingStyle){
        line(0, 0, new Float(r(animatet2)*Math.cos(animatet2)), new Float(r(animatet2)*Math.sin(animatet2)));
      }else{
        line(0, 0, new Float(r(animatet2)*Math.cos(animatet2)), new Float(-r(animatet2)*Math.sin(animatet2)));

      }
  }

  stroke(0);
  fill(255);



  popMatrix();
  state=WAITING;
}


//draw the right sidebar
public void drawInfoPanel() {
  //info panel
  textSize(12);
  noStroke();
  fill(200);
  rect(infopanelx, 0, width-infopanelx, height);
  fill(0);
  text(r, infopanelx+15, 20);
  text("\u03b81="+convertDecimalToMultipleOfPi(theta1), infopanelx+15, 40);
  text("\u03b82="+convertDecimalToMultipleOfPi(theta2), infopanelx+15, 55);
  //animate button
  fill(0, 0, 100);
  rect(infopanelx+15, height-50, width-infopanelx-30, 40);
  fill(255);
  textSize(20);
  text("Step \u2192", infopanelx+60, height-25);
}

public double convertRadiansToDegrees(double rad){
  return rad*180/PI;
}

//converts double to multiple of PI format
public String convertDecimalToMultipleOfPi(double x) {
  x/=PI;
  if (x < 0) {
    return "-" + convertDecimalToMultipleOfPi(-x);
  }
  if (x==0) {
    return "0";
  }
  double tolerance = 1.0e-6f;
  double h1=1;
  double h2=0;
  double k1=0;
  double k2=1;
  double b = x;
  do {
    double a = Math.floor(b);
    double aux = h1;
    h1 = a*h1+h2;
    h2 = aux;
    aux = k1;
    k1 = a*k1+k2;
    k2 = aux;
    b = 1/(b-a);
  } while (Math.abs(x-h1/k1) > x*tolerance);
  if (k1==1) {
    if (h1==1) {
      return "\u03c0";
    }
    return noDec.format(h1)+"\u03c0";
  }
  if (h1==1) {
    return "\u03c0/"+noDec.format(k1);
  }
  return noDec.format(h1)+"\u03c0/"+noDec.format(k1);
}

public void mouseClicked() {

    state=ANIMATE;
    wait=false;

}

public void keyPressed() {
  if (key==' ') {
    state=ANIMATE;
    wait=false;
  }
}
  public void settings() {  size(1200, 900); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "polar_visualizer" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
