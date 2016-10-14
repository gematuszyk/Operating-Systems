
public class Process implements Comparable<Process>{
	
	private int processFinishTime=0;
    private int IOtime;
    private int waitingTime;
    
//    public int ready;
//    public int blocked;
//    public int running;
//    public int notcreated;
    
    public int state;
    
    private int A,B,C,M;
    
    public Process(int a, int b, int c,int  m) {
    	this.A = a;
    	this.B = b;
    	this.C = c;
    	this.M = m;
    	state = -1;
    }
  
    public int getA() {
        return A;
    }
    
    public void setA(int a) {
        this.A = a;
    }
    public int getB() {
        return B;
    }
    public void setB(int b) {
        this.B = b;
    }
    public int getC() {
        return C;
    }
    
    public void setC(int c) {
        this.C = c;
    }
    
    public int getM() {
        return M;
    }
    
    public void setM(int m) {
        this.M = m;
    }
    public int getState() {
    	return state;
    }
    public void setState(int s) {
    	state = s;
    }
	public int getProcessFinishTime() {
		return processFinishTime;
	}
	public void setProcessFinishTime(int finish) {
		processFinishTime = finish;
	}
	public int getIOtime() {
		return IOtime;
	}
	public void setIOtime(int ioBurst) {
		IOtime = ioBurst;
	}
	public int getWaitingTime() {
		return waitingTime;
	}
	public void setWaitingTime(int wait) {
		waitingTime = wait;
	}
	
    
    public void increaseIOtime(int i){
		this.IOtime+=i;
	}
	public void increaseWaitingTime(int i){
		this.waitingTime+=i;
	}
	public void decreaseC(){
		this.C--;
	}
	

	@Override
	public int compareTo(Process o) {
		if(o.A>this.A)return -1;
		else if(o.A<this.A)return 1;
		else if(o.B>this.B)return -1;
		else if(o.B<this.B)return 1;
		else if(o.C>this.C)return -1;
		else if(o.C<this.C)return 1;
		else if(o.M>this.M)return -1;
		else if(o.M<this.M)return 1;
		else return 0;
	}


}
