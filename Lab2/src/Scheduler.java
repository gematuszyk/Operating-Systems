import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Scanner;

public class Scheduler {
	private static int numMixes;
	private static int time, runState, CPUburst;
	private static int[] blocked; 
	private static int[] bursts;
	private static boolean isFinished, verbose=false;
	
	private static ArrayList<Process> readyQueue;
	private static ArrayList<Process> potentialQueue;
	private ArrayList<Process> mainProcessList= new ArrayList<Process>();
	
	private static BufferedReader randomNum;
	//private static Hashtable<Integer, Integer> table;
	
	public static void main(String args[]) throws IOException{
		Scheduler schedule=new Scheduler();
		Scanner input = newScanner(args[0]);	
		if(args.length==2){
			Scanner input2 = newScanner(args[1]);
			verbose=true;
			schedule.createProcessList(input2);
		}else schedule.createProcessList(input);
		schedule.runFCFS();
		schedule.runRR();
		schedule.runUniprogrammed();
		schedule.runSJF();
	}
	
	private static int randomOS() throws NumberFormatException, IOException{
		randomNum = new BufferedReader(new FileReader("/Users/gracematuszyk/Desktop/random-numbers"));
		int num=Integer.parseInt(randomNum.readLine());
		return num;
	}

	
	private void runFCFS() throws NumberFormatException, IOException{
		int runsCompleted=0;
		double cpuTime=0,ioTime=0;
		time=0;
		runState=-1;
		//randomNum = new BufferedReader(new FileReader("/Users/gracematuszyk/Desktop/random-numbers"));
		isFinished=false;
		readyQueue= new ArrayList<Process>();
		potentialQueue= new ArrayList<Process>();
		blocked = new int[numMixes];
		ArrayList<Process> processList=new ArrayList<Process>();
		for(Process process:mainProcessList){
			processList.add(new Process(process.getA(),process.getB(),process.getC(),process.getM()));
		}
		for(int i=0;i<numMixes;i++)
			blocked[i]=-1;
 		while(!isFinished){
			if(verbose){
				runVerbose(processList);
			}
			checkReady(processList, numMixes, readyQueue);
			//	set the process to ready
			if(runState==-1){
				if(!readyQueue.isEmpty()){
				readyQueue.get(0).setState(1);
				runState=processList.indexOf(readyQueue.get(0));
				CPUburst=1+(randomOS()%processList.get(runState).getB());
				readyQueue.remove(0);
				}
			}
			//	run and block process
			else if(runState!=-1 && CPUburst==1){
				if(processList.get(runState).getC()!=1){
					runAndBlock(processList);				
				}
				else{
					if(processList.get(runState).getProcessFinishTime()==0){
						processList.get(runState).setProcessFinishTime(time);
					}
					processList.get(runState).decreaseC();
					processList.get(runState).setState(-2);				
				}
				if(!readyQueue.isEmpty()){
					runState=processList.indexOf(readyQueue.get(0));
					processList.get(runState).setState(1);
					readyQueue.remove(0);
					CPUburst=1+randomOS()%processList.get(runState).getB();
				}
				else
					runState=-1;
			}
			//	process ran	
			else if(runState!=-1 && CPUburst>0){
				CPUburst--;
				processList.get(runState).decreaseC();
			}
						
			for(int i=0;i<numMixes;i++){
				if(blocked[i]>0){
					blocked[i]--;
				}
			}
			runsCompleted=0;
			for(Process process:processList){
				if(process.getC()==0){
					if(processList.indexOf(process)==runState){
						if(!readyQueue.isEmpty()){
							runState=processList.indexOf(readyQueue.get(0));
							processList.get(runState).setState(1);
							readyQueue.remove(0);
							CPUburst=1+randomOS()%processList.get(runState).getB();
						}
					}
					runsCompleted++;
					if(process.getProcessFinishTime()==0){
						process.setProcessFinishTime(time);
					}
					process.setState(-2);
					readyQueue.remove(process);
				}
				if(runsCompleted==numMixes){
					isFinished=true;
				}
			}
			for(Process process:processList){
				if(process.getState()==0)process.increaseWaitingTime(1);
			}
			time++;
			if(runState!=-1)cpuTime++;
			for(int i=0; i<numMixes; i++){
				if(blocked[i]>=0){
					ioTime++;
					break;
				}
			}
			
		}
 		String name = "\n\nThe scheduling algorithm used was First Come First Served";
 		printOutput(processList, cpuTime, ioTime, name);
 		//randomNum.close();
 	}
	private void runRR() throws NumberFormatException, IOException{
		int runsCompleted=0;
		int quantum=2;
		double cpuTime=0,ioTime=0;
		time=0;
		runState=-1;
		//randomNum = new BufferedReader(new FileReader("/Users/gracematuszyk/Desktop/random-numbers"));
		isFinished=false;
		readyQueue= new ArrayList<Process>();
		potentialQueue= new ArrayList<Process>();
		blocked = new int[numMixes];
		bursts = new int[numMixes];
		ArrayList<Process> processList=new ArrayList<Process>();
		for(Process process:mainProcessList){
			processList.add(new Process(process.getA(),process.getB(),process.getC(),process.getM()));
		}
		for(int i=0;i<numMixes;i++){
			blocked[i]=-1;
			bursts[i]=0;
		}
 		while(!isFinished){
			if(verbose){
				runVerbose(processList);
			}
			checkReady(processList, numMixes, potentialQueue);
							
			if(runState==-1){
				if(!readyQueue.isEmpty()){
				readyQueue.get(0).setState(1);
				runState=processList.indexOf(readyQueue.get(0));
				if(bursts[runState]==0)
				bursts[runState]=1+randomOS()%processList.get(runState).getB();
				readyQueue.remove(0);
				quantum=2;

				}else if(!potentialQueue.isEmpty()){
					potentialQueue.get(0).setState(1);
					runState=processList.indexOf(potentialQueue.get(0));
					if(bursts[runState]==0)
					bursts[runState]=1+randomOS()%processList.get(runState).getB();
					potentialQueue.remove(0);
					quantum=2;

				}
			}
			else if(runState!=-1 && bursts[runState]==1){
				if(processList.get(runState).getC()!=1){	
					processList.get(runState).setState(2);
					bursts[runState]--;
					processList.get(runState).decreaseC();
					blocked[runState]=1+randomOS()%processList.get(runState).getM();
					processList.get(runState).increaseIOtime(blocked[runState]);
				}
				else{
					if(processList.get(runState).getProcessFinishTime()==0){
						processList.get(runState).setProcessFinishTime(time);
					}
					processList.get(runState).decreaseC();
					processList.get(runState).setState(-2);
					
				}
				if(!readyQueue.isEmpty()){
					runState=processList.indexOf(readyQueue.get(0));
					processList.get(runState).setState(1);
					readyQueue.remove(0);
					if(bursts[runState]==0)
					bursts[runState]=1+randomOS()%processList.get(runState).getB();
					quantum=2;
				}else if(!potentialQueue.isEmpty()){
					potentialQueue.get(0).setState(1);
					runState=processList.indexOf(potentialQueue.get(0));
					if(bursts[runState]==0)
					bursts[runState]=1+randomOS()%processList.get(runState).getB();
					potentialQueue.remove(0);	
					quantum=2;
				}
				else
					runState=-1;
			}
				
			else if(runState!=-1 && bursts[runState]>0){
				if(quantum==1){
					processList.get(runState).decreaseC();
					processList.get(runState).setState(0);
					bursts[runState]--;
					potentialQueue.add(processList.get(runState));
					HelperRR(processList, quantum);
				}else{
					quantum--;
					bursts[runState]--;
					processList.get(runState).decreaseC();
				}
			}
			Collections.sort(potentialQueue);
			readyQueue.addAll(potentialQueue);
			potentialQueue.clear();
			for(int i=0;i<numMixes;i++){
				if(blocked[i]>0){
					blocked[i]--;
				}
			}
			runsCompleted=0;
			for(Process process:processList){
				if(process.getC()==0){
					if(processList.indexOf(process)==runState){
						HelperRR(processList, quantum);
					}
					runsCompleted++;
					if(process.getProcessFinishTime()==0){
						process.setProcessFinishTime(time);
					}
					process.setState(-2);
					readyQueue.remove(process);
				}
				if(runsCompleted==numMixes){
					isFinished=true;
				}
			}
			for(Process process:processList){
				if(process.getState()==0)process.increaseWaitingTime(1);
			}
			time++;
			if(runState!=-1)cpuTime++;
			for(int i=0; i<numMixes; i++){
				if(blocked[i]>=0){
					ioTime++;
					break;
				}
			}
		}
 		String name = "The scheduling algorithm used was Round Robin";
 		printOutput(processList, cpuTime, ioTime, name);
		//randomNum.close();
 	}

	
	private void runUniprogrammed() throws NumberFormatException, IOException{
		boolean firstGo=false;
		int runsCompleted=0;
		double cpuTime=0,ioTime=0;
		time=0;
		runState=-1;
		//randomNum = new BufferedReader(new FileReader("/Users/gracematuszyk/Desktop/random-numbers"));
		isFinished=false;
		readyQueue= new ArrayList<Process>();
		potentialQueue= new ArrayList<Process>();
		blocked = new int[numMixes];
		ArrayList<Process> processList=new ArrayList<Process>();
		for(Process process:mainProcessList){
			processList.add(new Process(process.getA(),process.getB(),process.getC(),process.getM()));
		}
		for(int i=0;i<numMixes;i++)
			blocked[i]=-1;
 		while(!isFinished){
 			firstGo=false;
			if(verbose){
				runVerbose(processList);
			}
			if(runState!=-1){
			if(blocked[runState]==0){
					blocked[runState]--;
					if(processList.get(runState).getState()!=-2){
							processList.get(runState).setState(1);
							CPUburst=1+randomOS()%processList.get(runState).getB(); 
							firstGo=true;
					}
				}
			}
			for(Process process:processList){
				if(process.getA()==time){
					if(runState==-1){
						runState=processList.indexOf(process);
						CPUburst=1+randomOS()%process.getB(); 
						firstGo=true;
						process.setState(1);
					}
					else process.setState(0);
				}
			}		
			if(runState!=-1 && CPUburst==1 && !firstGo){
				if(processList.get(runState).getState()==1){
					if(processList.get(runState).getC()!=1){
						runAndBlock(processList);					
					}
					else{
						if(processList.get(runState).getProcessFinishTime()==0){
							processList.get(runState).setProcessFinishTime(time);
						}
						processList.get(runState).decreaseC();
						processList.get(runState).setState(-2);
						if(runState==processList.size()-1){
								isFinished=true;
						} else if(processList.get(runState+1).getState()==0){
								runState++;
								processList.get(runState).setState(1);
								CPUburst=1+randomOS()%processList.get(runState).getB(); 
						} else runState=-1;
				   }
				}
			}
				
			else if(runState!=-1 && CPUburst>1 && !firstGo){
				if(processList.get(runState).getState()==1){
					if(processList.get(runState).getC()==1){
						if(processList.get(runState).getProcessFinishTime()==0){
							processList.get(runState).setProcessFinishTime(time);
						}
						processList.get(runState).decreaseC();
						processList.get(runState).setState(-2);
						if(runState==processList.size()-1){
							isFinished=true;
						}
						else if(processList.get(runState+1).getState()==0){
							runState++;
							processList.get(runState).setState(1);
							CPUburst=1+randomOS()%processList.get(runState).getB(); 
						}
						else runState=-1;
					}else{
					CPUburst--;
					processList.get(runState).decreaseC();
					}
				}
			}
			if(blocked[runState]>0){
				blocked[runState]--;
			}
			runsCompleted=0;
			for(Process process:processList){
				if(process.getC()==0){
					runsCompleted++;
				}
				if(runsCompleted==processList.size()){
					isFinished=true;
				}
			}
			boolean didRun=false;
			for(Process process:processList){
				if(process.getState()==1)didRun=true;
				if(process.getState()==0)process.increaseWaitingTime(1);
			}
			if(didRun)cpuTime++;
			time++;
			for(int i=0; i<numMixes; i++){
				if(blocked[i]>=0){
					ioTime++;
					break;
				}
			}
		}
 		String name = "The scheduling algorithm used was Uniprogrammed";
 		printOutput(processList, cpuTime, ioTime, name);
 		//randomNum.close();
 	}
	private void runSJF() throws NumberFormatException, IOException{	
		ArrayList<Process> processList=new ArrayList<Process>();
		time=0;
		runState=-1;
		//randomNum = new BufferedReader(new FileReader("/Users/gracematuszyk/Desktop/random-numbers"));
		isFinished=false;
		readyQueue= new ArrayList<Process>();
		potentialQueue= new ArrayList<Process>();
		for(Process process:mainProcessList){
			processList.add(new Process(process.getA(),process.getB(),process.getC(),process.getM()));
		}
		
		int runsCompleted=0;
		double cpuTime=0,ioTime=0;
		blocked = new int[numMixes];
		for(int i=0;i<numMixes;i++)
			blocked[i]=-1;
 		while(!isFinished){
			if(verbose){
				runVerbose(processList);
			}
			checkReady(processList, numMixes, readyQueue);
					
			if(runState==-1){
				if(!readyQueue.isEmpty()){
				int i=0,min=100000,runInd=-1;
				for(;i<readyQueue.size();i++){
					if(readyQueue.get(i).getC()<min){
						min=readyQueue.get(i).getC();
						runInd=i;
					}
				}
				readyQueue.get(runInd).setState(1);
				runState=processList.indexOf(readyQueue.get(runInd));
				CPUburst=1+randomOS()%processList.get(runState).getB();
				readyQueue.remove(runInd);
				}
			}
					
			else if(runState!=-1 && CPUburst==1){
				if(processList.get(runState).getC()!=1){	
					runAndBlock(processList);
				}
				else{
					if(processList.get(runState).getProcessFinishTime()==0){
						processList.get(runState).setProcessFinishTime(time);
					}
					processList.get(runState).setState(-2);
					processList.get(runState).decreaseC();				
				}
				if(!readyQueue.isEmpty()){
					int i=0,min=100000,runInd=-1;
					for(;i<readyQueue.size();i++){
						if(readyQueue.get(i).getC()<min){
							min=readyQueue.get(i).getC();
							runInd=i;
						}
					}
					readyQueue.get(runInd).setState(1);
					runState=processList.indexOf(readyQueue.get(runInd));
					CPUburst=1+randomOS()%processList.get(runState).getB();
					readyQueue.remove(runInd);
					}
				else
					runState=-1;
			}
			
			else if(runState!=-1 && CPUburst>0){
				CPUburst--;
				processList.get(runState).decreaseC();
			}
						
			for(int i=0;i<numMixes;i++){
				if(blocked[i]>0){
					blocked[i]--;
				}
			}			
			runsCompleted=0;
			for(Process process:processList){
				if(process.getC()==0){
					if(processList.indexOf(process)==runState){
						if(!readyQueue.isEmpty()){
							int i=0,min=100000,runInd=-1;
							for(;i<readyQueue.size();i++){
								if(readyQueue.get(i).getC()<min){
									min=readyQueue.get(i).getC();
									runInd=i;
								}
							}
							readyQueue.get(runInd).setState(1);
							runState=processList.indexOf(readyQueue.get(runInd));
							CPUburst=1+randomOS()%processList.get(runState).getB();
							readyQueue.remove(runInd);
							}
						else runState=-1;
					}
					runsCompleted++;
					if(process.getProcessFinishTime()==0){
						process.setProcessFinishTime(time);
					}
					process.setState(-2);
					readyQueue.remove(process);
				}
				if(runsCompleted==numMixes){
					isFinished=true;
				}
			}
			for(Process process:processList){
				if(process.getState()==0)process.increaseWaitingTime(1);
			}
			time++;
			if(runState!=-1)cpuTime++;
			for(int i=0; i<numMixes; i++){
				if(blocked[i]>=0){
					ioTime++;
					break;
				}
			}
			if(time==200)break;
		}
 		String name = "The scheduling algorithm used was Shortest Job First";
 		printOutput(processList, cpuTime, ioTime, name);
 		//randomNum.close();
 	}
	//eliminate redundant code methods
	public static void HelperRR(ArrayList<Process> processList, int quantum) throws NumberFormatException, IOException {
		if(!readyQueue.isEmpty()){
			runState=processList.indexOf(readyQueue.get(0));
			processList.get(runState).setState(1);
			readyQueue.remove(0);
			if(bursts[runState]==0)
			bursts[runState]=1+randomOS()%processList.get(runState).getB();
			quantum=2;
		}else if(!potentialQueue.isEmpty()){
			potentialQueue.get(0).setState(1);
			runState=processList.indexOf(potentialQueue.get(0));
			if(bursts[runState]==0)
			bursts[runState]=1+randomOS()%processList.get(runState).getB();
			potentialQueue.remove(0);	
			quantum=2;
		}
	}
	
	public static void runAndBlock(ArrayList<Process> processList) throws NumberFormatException, IOException {
		processList.get(runState).setState(2);
		processList.get(runState).decreaseC();
		blocked[runState]=1+randomOS()%processList.get(runState).getM();
		processList.get(runState).increaseIOtime(blocked[runState]);
	}
	
	public static void checkReady(ArrayList<Process> processList, int numMixes, ArrayList<Process> queue) {
		for(int i=0;i<numMixes;i++){
			if(blocked[i]==0){
				blocked[i]--;
				if(processList.get(i).getState()!=-2){
					processList.get(i).setState(0);
					queue.add(processList.get(i));
				}
			}
		}
		for(Process process:processList){
			if(process.getA()==time){
				process.setState(0);
				queue.add(process);
			}
		}
	}
	
	public static Scanner newScanner(String fileName){
	    try{
	      Scanner input = new Scanner(new BufferedReader(new FileReader(fileName)));
	      return input;
	      
	    }
	    catch(Exception ex){
	      System.out.printf("Error reading %s\n", fileName);
	      System.exit(0);
	    }
	    return null;
	  }
 	

	public void createProcessList(Scanner path) throws IOException{	
		int arrivalTime,b,cpuTime,m;
		numMixes = path.nextInt();
		System.out.print("The original input was: " + numMixes +  " ");
		  
		do {
		      Integer[] text = new Integer[4];
		      for (int i = 0; i < 4; i++) {
		          if (path.hasNext()) {        
		              text[i] = path.nextInt();
		          }          
		      }
		      arrivalTime = new Integer(text[0]);
		      b = new Integer(text[1]);
		      cpuTime = new Integer(text[2]);
		      m = new Integer(text[3]);
		      Process process = new Process(arrivalTime, b, cpuTime, m);
		      mainProcessList.add(process);
		      System.out.print("( " + arrivalTime + ", " + b + ", "+ cpuTime + ", "+ m + " ) ");
		  } while (path.hasNext());		
		Collections.sort(mainProcessList);	
		System.out.print("\nThe (sorted) input is:  " + numMixes +  " ");
		for(Process process:mainProcessList){
			System.out.print("( " + process.getA() + ", " + process.getB() + ", "+ process.getC() + ", "+ process.getM() + " ) ");			
		}
	}
	

	public void runVerbose(ArrayList<Process> processList) {
		System.out.print("Before cycle " + time + "  :	");
			for(Process process:processList){
				if(process.getState()==-2){
					System.out.print("Terminated 0	");
				}
				if(process.getState()==-1){
					System.out.print("Unstarted 0	");
				}
				if(process.getState()==0){
						System.out.print("Ready 0		");
				}
				if(process.getState()==1){
					System.out.print("Running "+ CPUburst+ "	");
				}
				if(process.getState()==2){
					System.out.print("Blocked " + blocked[processList.indexOf(process)]+"	\n");
				}
			}
	}

	public void printOutput(ArrayList<Process> processList, double cpuTime, double ioTime, String name) {
		System.out.println(name);
		System.out.println();
			int count=0;
			double avgWait=0,avgTime=0,max=-1;
			for(Process process:processList){
				if(process.getProcessFinishTime()>max)max=process.getProcessFinishTime();
				avgWait+=process.getWaitingTime();
				avgTime+=process.getProcessFinishTime()-process.getA();
				System.out.println("Process "+ count++ +":");
				System.out.println("\t(A,B,C,IO) = ("+process.getA()+ ","+process.getB()+","+ (process.getProcessFinishTime()-process.getA()-process.getIOtime()-process.getWaitingTime())+","+process.getM()+")");
				System.out.println("\tFinishing time: "+ process.getProcessFinishTime());
				System.out.println("\tTurnaround time: "+(process.getProcessFinishTime()-process.getA()));
				System.out.println("\tI/O time: " + process.getIOtime());
				System.out.println("\tWaiting time: " + process.getWaitingTime()+"\n");
			}
			System.out.println("Summary Data:");
			System.out.println("\tFinishing time: "+ max);
			System.out.println("\tCPU Utilization: "+ cpuTime/max);
			System.out.println("\tI/O Utilization: "+ ioTime/max);
			System.out.println("\tThroughput "+ (count/max)*100 +" processes per hundread cycles");
			System.out.println("\tAverage Turnaround time " + avgTime/count);
			System.out.println("\tAverage Wait time "+ avgWait/count+"\n"); 
			System.out.println("------------------------------------------------------------------------------------------");
	
	}
}
