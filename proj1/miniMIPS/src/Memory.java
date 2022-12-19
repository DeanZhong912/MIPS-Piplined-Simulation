import java.util.Vector;

public class Memory {
    public Vector<String> vector = new Vector<String>();
    public Vector<String> pushVector(String string){
        this.vector.add(string);
        return vector;
    }

    public String getVector(int index){
        return vector.get(index);
    }

    public void modifyVector(int index,String string){
        this.vector.set(index,string);
    }

    public int getVectorSize(){
        return this.vector.size();
    }

    public void printData(Vector<String> vector){
        String string;
        for(int i = 0;i<24;i++){
            if(i==8||i==16){
                System.out.print("\n");
            }
            string = String.valueOf(Method.binaryComplementToDecimal(vector.get(i)));
            System.out.print(string+" ");
        }
    }
}
