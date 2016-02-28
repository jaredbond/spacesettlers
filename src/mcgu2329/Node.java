package mcgu2329;

import java.util.ArrayList;

import spacesettlers.utilities.Position;

public class Node {
	int x;
	int y;
	double Gn, Hn;
	boolean seen;
	ArrayList<Node> adjNodes;
	Node parentNode;
	Position center;

	public Node(int x, int y, Position p) {
		this.x = x;
		this.y = y;
		seen = false;
		adjNodes = new ArrayList<Node>();
		this.Gn = 0;
        this.Hn = 0;
		parentNode = null;
		center = p;
	}
    
    public Node(int x, int y, double Hn) {
        this.x = x;
        this.y = y;
        seen = false;
        adjNodes = new ArrayList<Node>();
        this.Gn = 0;
        this.Hn = Hn;
        parentNode = null;
    }
    
    public Node(int x, int y, double Gn, double Hn) {
        this.x = x;
        this.y = y;
        seen = false;
        adjNodes = new ArrayList<Node>();
        this.Gn = Gn;
        this.Hn = Hn;
        parentNode = null;
    }
    
	public int getX() {
		return x;
	}
	public int getY() {
		return y;
	}
	public void setSeen() {
		seen = true;
	}
	public void setUnseen() {
		seen = false;
	}
	public boolean getSeen() {
		return seen;
	}
	public void addNode(Node n) {
		adjNodes.add(n);
	}
	public double getHn() {
		return Hn;
	}
	public void setHn(double h) {
		Hn = h;
	}
	public double getGn() {
		return Gn;
	}
	public void setGn(double g) {
		Gn = g;
	}
	public double getFn() {
		return getHn()+getGn();
	}
	public Node getParent() {
		return parentNode;
	}
	public void setParent(Node n) {
		parentNode = n;
	}
	public ArrayList<Node> getAdjNodes() {
		return adjNodes;
	}
	public Position getCenter() {
		return center;
	}
	public void setCenter(Position p) {
		center = p;
	}
	public String toString() {
		return "(" + y + "," + x + ")";
	}
}