import java.awt.event.MouseEvent;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.*;
import java.text.DecimalFormat;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.Collections;
import java.util.InputMismatchException;
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.stream.IntStream;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.Axis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
public class Stock extends Application
{
	int size = 47;
	int percChange = 0;
	public static void main(String[] args) 
	{
		launch(args);
	}
	Pane root;
	Pane pane;
	static Calendar calender = Calendar.getInstance();
	java.util.Date date = new java.util.Date(); // grabs the current date 
	String temp = String.valueOf(date);
	String[] day = temp.split("\\s+");
	public void start(Stage stage) 
	{
		stage.setTitle("Stock Tracker Widget");
		stage.initStyle(StageStyle.TRANSPARENT);
		root = new Pane();

		Rectangle myPane = new Rectangle(0, 0, 420, 700); // my pane to give the illusion of rounded corners
		myPane.setArcHeight(10);
		myPane.setArcWidth(10);
		root.getChildren().add(myPane);
		myPane.setFill(new RadialGradient( // gradient color
				0, 0, 0, 0, 1, true,                  //sizing
				CycleMethod.NO_CYCLE,                 //cycling
				new Stop(0, Color.web("0x2b2f3b",1.0)),    //colors
				new Stop(1, Color.web("0x2b2f3b",1.0)))
				);

		Scene scene = new Scene(root, 420, 700);
		scene.getStylesheets().add("style.css"); // css for the textfield
		pane = new Pane(); // pane to hold the date
		root.getChildren().add(pane);
		scene.setFill(Color.TRANSPARENT);

		int month = (int)calender.get(Calendar.MONTH);
		month++;
		int myDate = calender.get(Calendar.DATE);
		myDate--;
		int year = calender.get(Calendar.YEAR);
		addDateandTitle(); // adds the date and the title of the widget
		stage.setOpacity(0.95);
		stage.setScene(scene);
		root.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY)));
		stage.show();

		Integer stocks = null;
		int count[] = {0}, mont[] = {month}, myDay[] = {myDate}; // doing array else I get a run time error 
		
		TextField tf = new TextField(); // my searchbar
		tf.setLayoutX(241);
		tf.setLayoutY(37);
		root.getChildren().add(tf);
		boolean wrong[] = {false}; // used an array because I was getting runtime errors 
		tf.setPromptText("Enter stock symbol here");
		tf.setOnAction((ActionEvent ae) -> 
		{
			wrong[0] = false; // this boolean tells me if anything went wrong in the api calls
			String ticker = "", exchange = "";
			ticker = tf.getText();
			ticker = ticker.toUpperCase();
			Double prevDay = 0.0;
			String price = null;
			try {
				price = getPrice(ticker, exchange);
				if(!isNumeric(price) || price.isEmpty())
				{
					wrong[0] = true;
				}
			}
			catch (IOException e) {
				e.printStackTrace();
			}

			try {
				prevDay = getPrevDayClose(ticker,  mont[0],  myDay[0],  year);
				if(prevDay == -1.0)
				{
					wrong[0] = true;
				}
				try
				{
					if(!wrong[0])populateChartValues(ticker,  mont[0],  myDay[0] + 1,  year, count);
				}
				catch(IndexOutOfBoundsException e)
				{
					wrong[0] = true;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			if(!wrong[0]) // only analyze data if in correct format
			{
				tf.setStyle("-fx-text-inner-color: BLACK;"); // using css to style my searchbar
				Double currPrice = Double.parseDouble(price);
				Double sum[] = {currPrice - prevDay};
				final DecimalFormat df = new DecimalFormat("0.00"); // formats to 2 deicmal places
				sum[0] = Double.parseDouble(df.format(sum[0]));
				Double percentChange[] = {sum[0]/prevDay};
				percentChange[0] *= 100;
				percentChange[0] = Double.parseDouble(df.format(percentChange[0]));
				
				String len = String.valueOf(sum[0]) + String.valueOf(percentChange[0]);
				int currLen = len.length();
				Button percChange[] = {new Button(String.valueOf("$" + sum[0]))};
				percChange[0].setTranslateX(308);
				percChange[0].setTranslateY(size*count[0]+ 90);
				percChange[0].setMinWidth(currLen*6);
				percChange[0].setMinHeight(25);
				percChange[0].setTextFill(Color.WHITE);
				boolean green;
				if(currPrice < prevDay) // this means that the stock is down
				{
					percChange[0].setStyle("-fx-background-color: DARKRED;");
					green = false;
				}
				else // this means that the stock is up
				{
					percChange[0].setStyle("-fx-background-color: DARKGREEN;");
					green = true;
				}
				
				percChange[0].setOnMouseEntered(evt -> { // this is for the animation of the rectangle        
					percChange[0].setText( percentChange[0] + "%");
				});
				percChange[0].setOnMouseExited(evt -> { // this is for the animation of the rectangle  
					percChange[0].setText("$" + String.valueOf(sum[0]));
				});

				root.getChildren().add(percChange[0]);
				Label stock = new Label(ticker + ": $" + price);
				stock.setLayoutX(17);
				stock.setLayoutY(size*count[0] + 92);
				stock.setStyle("-fx-font: 20 arial;"); // setting the font
				stock.setTextFill(Color.WHITESMOKE);
				root.getChildren().add(stock);
				Line border2 = new Line(10, size*count[0] + 78,400, size*count[0] + 78);// creating a border b/w stocks
				Line border = new Line(10, size*count[0] + 125,400, size*count[0] + 125); // creating a border
				border.setFill(Color.WHITE);
				border2.setFill(Color.WHITE);
				root.getChildren().add(border);
				root.getChildren().add(border2);
				count[0]++;
				tf.clear(); // clears the textfield once the stock is processed
				String voice;
				percChange[0].setFocusTraversable(true); // this is for the voiceOver 
				if(green)
				{
					voice = ticker + " is currently at " + price + " and it is up by " + sum[0] + " dollars";
				}
				else
				{
					voice = ticker + " is currently at $" + price + " and it is down by " + sum[0] + " dollars";
				}
				percChange[0].setAccessibleText(voice);
			}
			else // if something goes wrong with the stock then change the color of the search bar to red
			{
				tf.setStyle("-fx-text-inner-color: RED;");
			}
		}
				);
		addDragListeners(root, stage);
	}
	public void addDateandTitle() // adds date and title to the widget
	{

		Label a = new Label(day[0] + ", " + day[1] + " " + day[2]);
		a.setLayoutX(10);
		a.setLayoutY(10 + 27);
		a.setStyle("-fx-font: 20 arial;");
		a.setTextFill(Color.LIGHTGREY);
		pane.getChildren().addAll(a);

		Label title = new Label("Stocks");
		title.setLayoutY(10);
		title.setLayoutX(160 - 150);
		title.setStyle("-fx-font: 20 arial;");
		title.setTextFill(Color.WHITE);
		root.getChildren().add(title);
	}
	public static boolean isNumeric(String strNum) // tells me if a string is in the correct format
	{
		if (strNum == null) {
			return false;
		}
		try {
			double d = Double.parseDouble(strNum);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}
	double x, y;
	private void addDragListeners(final Node n, Stage primaryStage){ // creates a draggable rectangle

		n.setOnMousePressed(( mouseEvent) -> {
			this.x = n.getScene().getWindow().getX() - mouseEvent.getScreenX();
			this.y = n.getScene().getWindow().getY() - mouseEvent.getScreenY();
		});

		n.setOnMouseDragged(( mouseEvent) -> {
			primaryStage.setX(mouseEvent.getScreenX() + this.x);
			primaryStage.setY(mouseEvent.getScreenY() + this.y);
		});
	}
	public static String getPrice(String tick, String exc) throws IOException // gets the current price of the stock
	{
		URL url = new URL("https://financialmodelingprep.com/api/v3/discounted-"
				+ "cash-flow/" + tick + "?apikey=a37164d3ae2260b868b741bcd8c38f5d");
		URLConnection urlConn = url.openConnection();
		InputStreamReader inStream = new InputStreamReader(urlConn.getInputStream());
		BufferedReader buff = new BufferedReader(inStream);

		// bug when the stock ends in a ten decimal 
		String price = "not found";
		String line = buff.readLine();
		while(line != null) // parsing the json that is returned from the api
		{
			if(line.contains("\"Stock Price\" : "))
			{
				String tem = line.substring( line.indexOf("."), line.length() - 1);
				if(tem.length() == 1)
				{
					price = line.substring(line.indexOf("ce\" : ") + 6, line.indexOf(".") + 2);
				}
				else
				{
					price = line.substring(line.indexOf("ce\" : ") + 6, line.indexOf(".") + 3);
				}

			}
			line = buff.readLine();
		}
		System.out.println("price = " + price);
		return price;
	}
	public Double getPrevDayClose(String tic, int mon, int myDa, int ye) throws IOException // gets previous close day
	{
		LocalTime now = LocalTime.now();
		LocalTime bad = LocalTime.of(4, 0, 0);
		boolean isbefore = now.isBefore(bad);
		// dont know what will happen from 1 am to 6:30
		if(isbefore)
		{
			myDa--;
		}
		if(day[0].equals("Mon")) // on monday go back to friday to check prev close
		{
			myDa -= 2;
		}
		if(myDa == 0) // this happens when I try to get the previous day and it's the first day of the month (1 -> 0) but
			// there is no day 0 so have to go back to day 30. But not all months end in day 30 so this needs to be fixed
		{
			myDa = 30;
			mon--;
		}
		String check = "";
		if(myDa < 10 ) // string to check for the api
		{
			check = ye + "-" + mon + "-0" + myDa+ "\": {";
		}
		else
		{
			check = ye + "-" + mon + "-" + myDa+ "\": {";
		}
		URL url = new URL("https://www.alphavantage.co/query?function=TIME_SERIES_DAILY"
				+ "_ADJUSTED&symbol=" + tic + "&apikey=FC3VU5M4XJV75U6J");
		URLConnection urlConn = url.openConnection();
		InputStreamReader inStream = new InputStreamReader(urlConn.getInputStream());
		BufferedReader buff = new BufferedReader(inStream);

		String price = "not found";
		String line = buff.readLine();
		String prevClose = "";
		while(line != null) // parsing the json from the api 
		{
			if(line.contains(check))
			{
				line = buff.readLine();
				line = buff.readLine();
				line = buff.readLine();
				line = buff.readLine();
				line = buff.readLine();
				prevClose = line.substring(line.indexOf(": \"") + 3, line.lastIndexOf("\""));
				break;
			}
			line = buff.readLine();
		}
		if(prevClose.equals("")) // if empty string then did not find the stock 
		{
			System.out.println("Did not find the stock");
			return -1.0;
		}
		return Double.parseDouble(prevClose);
	}
	
	// here do the chart for each stock, last part!
	public void populateChartValues(String tick, int mon, int myDa, int ye, int count[]) throws IOException
	{
		URL url = new URL("https://api.twelvedata.com/time_series?symbol=" + tick + 
				"&interval=15min&apikey=8bed048a9c1c4b15866285e0aab7b03e&source=docs");
		URLConnection urlConn = url.openConnection();
		InputStreamReader inStream = new InputStreamReader(urlConn.getInputStream());
		BufferedReader buff = new BufferedReader(inStream);

		List<Double> listForGraph = new ArrayList<Double>();
		String price = "not found";
		String line = buff.readLine();
		String myLines[] = line.split("},");
		for(int i = 1; i < myLines.length; i++)
		{
			if(myDa < 10)
			{
				if(myLines[i].contains(ye + "-" + mon + "-0" + myDa))
				{
					listForGraph.add(Double.parseDouble(myLines[i].substring(myLines[i].
							indexOf("open\":\"") + 7, myLines[i].indexOf(".") + 6)));
				}
			}
			else
			{
				if(myLines[i].contains(ye + "-" + mon + "-" + myDa))
				{
					listForGraph.add(Double.parseDouble(myLines[i].substring(myLines[i].
							indexOf("open\":\"") + 7, myLines[i].indexOf(".") + 6)));
				}
			}
			
		}
		ArrayList<Double> revArrayList = new ArrayList<Double>();
		for (int i = listForGraph.size() - 1; i >= 0; i--) { // reversing the array
			revArrayList.add(listForGraph.get(i));
		}
		listForGraph = revArrayList;
//		for(int i = 0; i < listForGraph.size(); i++)
//		{
//			System.out.println(listForGraph.get(i));
//		}
		createChart(listForGraph, count); 
	}
	public Double normalize(double x, double dataHigh, double dataLow) { // normalizes a point so all charts are of the same size
		return ((x - dataLow) / (dataHigh - dataLow)) * (60 - 30) + 30;
	}
	public void createChart(List<Double> graph, int count[]) // creates a chart from a list of data points
	{
		ArrayList<Double> tempSort = new ArrayList<Double>(graph);
		Collections.sort(tempSort); // sorts the list to find the smallest and largest value to normalize
		Double min = tempSort.get(0), max = tempSort.get(tempSort.size() - 1);

		List<Point> points = new ArrayList<Point>();
		for(int i = 0; i < graph.size(); i++) // initialize the list with the values from the parameter and normalize them
		{
			points.add(new Point(i*4, normalize(graph.get(i), max, min)));
//			System.out.println(points.get(i));
		}

		int increm = -1 * points.get(0).x - 2;
		for(int i = 0; i < points.size() - 1; i++)
		{
			// here I reflect the line over the x-axis and add them to my root
			Line temp = new Line(points.get(i).x, -1 * points.get(i).y - increm, points.get(i + 1).x, -1 * points.get(i + 1).y - increm);
//			System.out.println("Line = " + temp.getStartX() + " "+ temp.getStartY() + " "+ temp.getEndX() + " "+ temp.getEndX() + " ");
			temp.setLayoutX(185);
			temp.setLayoutY(size * count[0] + 140);
			temp.setStrokeWidth(2);
			root.getChildren().add(temp);
		}
	}
	class Point // class that makes it easier to store data points for my chart
	{
		int x;
		Double y;
		Point(int a, Double b) // constructor
		{
			x = a; 
			y = b;
		}
		@Override
		public String toString() { // helped me with debugging
			return "Point [x=" + x + ", y=" + y + "]";
		}
	}
}
