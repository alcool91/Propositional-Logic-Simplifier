package plogic;
import java.util.ArrayList;

class Stack<T> {
    node head;
    
    class node {
        T    value;
        node link;
        node() {};
        node(T value) {
            this.value=value;
        }
        public T    getValue(){return value;}
        public node getLink() {return link; }
        public void setValue(T value) {this.value=value;}
        public void setLink(node link){this.link=link;  }
    }
    public void empty() {
        head.setLink(null);
        head=null;
    }
    public boolean isEmpty() { return head == null; }
    public void push(T e) {
        node temp;
        temp = new node(e);
        if (this.isEmpty()) {
            head = temp;
        } else {
            temp.setLink(head);
            head=temp;
        }
    }
    public T pop() {
        node temp;
        temp = head;
        head = head.getLink();
        return temp.getValue();
    }
    public T peek() {
        return head.getValue();
    }
}

class Queue<T> {
    node head;
    node tail;
    
    class node{
        T    value;
        node link, back;
        node() {};
        node(T value) {
            this.value=value;
        }
        public T    getValue(){return value;}
        public node getLink() {return link; }
        public void setValue(T value) {this.value=value;}
        public void setLink(node link){this.link=link;  }
        public void setBack(node back){this.back=back; }
        public node getBack() {return back; }
    }
    public boolean isEmpty() {
        return (head == null);
    }
    public void enqueue(T t) {
        node temp;
        temp = new node(t);
        if (this.isEmpty()) tail=temp;
        else head.setBack(temp);
        temp.setLink(head);
        head = temp;  
    }
    public T dequeue() {
        if (head == tail) {
            node a = head;
            head = null;
            tail = null;
            return a.getValue();
        }
        node temp;
        temp = tail;
        tail = tail.getBack();
        tail.setLink(null);
        return temp.getValue();
    }
    public Queue<T> Copy() {
        Queue<T> n = new Queue<T>();
        node temp;
        temp = tail;
        while(temp!=null) {
            n.enqueue(temp.getValue());
            temp=temp.getBack();
        }
        return n;
    }
}

class TTBool {
    boolean value;
    TTBool(boolean value) { this.value = value; }
    public String toString() {
        if (value) return "T";
        else return "F";
    }
}

class Minterm {
    String term;
    ArrayList<Integer> value = new ArrayList<>();
    boolean checked = false;
    Minterm(String term) {
        int v = 0;
        this.term=term;
        if (!this.term.contains("X")) {
            for(int i = term.length()-1; i >= 0; i--) {
                int a = Integer.parseInt(Character.toString(term.charAt(i)));
                v+=a*((int)Math.pow(2, term.length()-1-i));
                
            }value.add(v);
        }
    }
}
