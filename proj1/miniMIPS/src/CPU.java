import java.io.PrintWriter;
import java.util.Vector;

public class CPU {
    private Memory memory = new Memory();
    private Instruction instruction = new Instruction();
    private int break_position;
    private Vector<Integer> registers = new Vector<Integer>();
    private int PC = 64;
    private int base_PC = 64;

    public void InitalRegister(){
        for(int i=0;i<32;i++){
            registers.add(0);
        }
    }

    CPU(Memory memory,int break_position){
        this.memory = memory;
        this.break_position = break_position;
    }

    public Vector<Integer> getRegisters(){return this.registers;}

    public void calculation(PrintWriter printWriter){
        int cycleNum = 1;
        int dataNum=0;
        int countData=0;
        InitalRegister();
        instruction = Method.transform(this.memory.getVector((PC-base_PC)/4));
        while(PC <= break_position&&!instruction.Function.equals("BREAK")){
            instruction = Method.transform(this.memory.getVector((PC-base_PC)/4));
            System.out.println("--------------------");
            System.out.println("Cycle:"+cycleNum+Instruction.writeInByType2(instruction,PC));
            System.out.print("\n");
            System.out.print("Registers\nR00:\t");
            PC = calculationDetails(instruction);
            printWriter.println("--------------------");
            printWriter.println("Cycle:"+cycleNum+Instruction.writeInByType2(instruction,PC));
            printWriter.print("\n");
            printWriter.print("Registers\nR00:\t");
            for(int i =0;i<32;i++){
                if(i==16){
                    System.out.print("\nR16:\t");
                    printWriter.print("\nR16:\t");
                }
                if(i==15||i==31){
                    System.out.print(registers.get(i));
                    printWriter.print(registers.get(i));
                }else{
                    System.out.print(registers.get(i)+"\t");
                    printWriter.print(registers.get(i)+"\t");
                }
            }

            System.out.print("\n\nData");
            printWriter.print("\n\nData");
            countData = 0;
            for(int i = (break_position-base_PC+4)/4;i<memory.getVectorSize();i++){
                if(countData%8==0){
                    System.out.print("\n"+4*i+base_PC+":\t");
                    printWriter.print("\n"+4*i+base_PC+":\t");
                }
                countData++;
                dataNum = Method.binaryComplementToDecimal(memory.getVector(i));
                if(countData%8==0){
                    System.out.print(dataNum);
                    printWriter.print(dataNum);
                }else{
                    System.out.print(dataNum+"\t");
                    printWriter.print(dataNum+"\t");
                }
            }

            System.out.print("\n\n");
            printWriter.print("\n\n");
            cycleNum++;
        }

    }

    public int calculationDetails(Instruction instruction){
        int rs,rt,rd,immediate,base,temp;
        switch (instruction.Function){
            case("ADD"):
                if(instruction.Type.equals("1")){
                    rs = registers.get(Integer.parseInt(instruction.getRs()));
                    rt = registers.get(Integer.parseInt(instruction.getRt()));
                    registers.set(Integer.parseInt(instruction.getRd()),rs+rt);
                }else if(instruction.Type.equals("2")){
                    rs = registers.get(Integer.parseInt(instruction.getRs()));
                    immediate =Integer.parseInt(instruction.getImmediate());
                    registers.set(Integer.parseInt(instruction.getRt()),rs+immediate);
                }
                PC+=4;
                break;
            case("SUB"):
                if(instruction.Type.equals("1")){
                    rs = registers.get(Integer.parseInt(instruction.getRs()));
                    rt = registers.get(Integer.parseInt(instruction.getRt()));
                    registers.set(Integer.parseInt(instruction.getRd()),rs-rt);
                }else if(instruction.Type.equals("2")){
                    rs = registers.get(Integer.parseInt(instruction.getRs()));
                    immediate = Integer.parseInt(instruction.getImmediate());
                    registers.set(Integer.parseInt(instruction.getRt()),rs-immediate);
                }
                PC+=4;
                break;
            case("BEQ"):
                rs = registers.get(Integer.parseInt(instruction.getRs()));
                rt = registers.get(Integer.parseInt(instruction.getRt()));
                if(rs==rt){
                    PC = Integer.parseInt(instruction.getImmediate())+PC+4;
                }else {
                    PC += 4;
                }
                break;
            case("SLL"):
                rt = registers.get(Integer.parseInt(instruction.getRt()));
                immediate = Integer.parseInt(instruction.getImmediate());
                registers.set(Integer.parseInt(instruction.getRd()),rt<<immediate);
                PC+=4;
                break;
            case("LW"):
                immediate = Integer.parseInt(instruction.getImmediate());
                base = registers.get(Integer.parseInt(instruction.getBase()));
                temp = Method.binaryComplementToDecimal(memory.getVector((immediate+base-base_PC)/4));
                registers.set(Integer.parseInt(instruction.getRt()),temp);
                PC+=4;
                break;
            case("SW"):
                rt = registers.get(Integer.parseInt(instruction.getRt()));
                immediate = Integer.parseInt(instruction.getImmediate());//offset的值 188
                base = registers.get(Integer.parseInt(instruction.getBase())); //R16寄存器里的值 0
                memory.modifyVector((base+immediate-base_PC)/4,Method.ComplementToBinary(rt));
                PC+=4;
                break;
            case("SRA"):
                rt = registers.get(Integer.parseInt(instruction.getRt()));
                immediate = Integer.parseInt(instruction.getImmediate());
                registers.set(Integer.parseInt(instruction.getRd()),rt>>immediate);
                PC+=4;
                break;
            case("SRL"):
                rt = registers.get(Integer.parseInt(instruction.getRt()));
                immediate = Integer.parseInt(instruction.getImmediate());
                registers.set(Integer.parseInt(instruction.getRt()),rt>>immediate);
                PC+=4;
                break;
            case("J"):
                immediate = Integer.parseInt(instruction.getImmediate());
                PC = immediate;
                break;
            case("JR"):
                rs = Integer.parseInt(instruction.getRs());
                PC=registers.get(rs);
                break;
            case("MUL"):
                rs = registers.get(Integer.parseInt(instruction.getRs()));
                rt = registers.get(Integer.parseInt(instruction.getRt()));
                registers.set(Integer.parseInt(instruction.getRd()),rs*rt);
                PC+=4;
                break;
            case("BGTZ"):
                rs = registers.get(Integer.parseInt(instruction.getRs()));
                if(rs>0){
                    PC = (Integer.parseInt(instruction.getImmediate())-2)*4+64;
                }else{
                    PC+=4;
                }
                break;
            case("BLTZ"):
                rs = registers.get(Integer.parseInt(instruction.getRs()));
                if(rs<0){
                    PC = (Integer.parseInt(instruction.getImmediate())-2)*4+64;
                }else{
                    PC+=4;
                }
                break;
            case("NOP"):
                PC+=4;
                break;
            case("AND"):
                rs = registers.get(Integer.parseInt(instruction.getRs()));
                rt = registers.get(Integer.parseInt(instruction.getRt()));
                registers.set(Integer.parseInt(instruction.getRd()),rs&rt);
                PC+=4;
                break;
            case("NOR"):
                rs = registers.get(Integer.parseInt(instruction.getRs()));
                rt = registers.get(Integer.parseInt(instruction.getRt()));
                registers.set(Integer.parseInt(instruction.getRd()),rs|rt);
                PC+=4;
                break;
            case("SLT"):
                rs = registers.get(Integer.parseInt(instruction.getRs()));
                rt = registers.get(Integer.parseInt(instruction.getRt()));
                registers.set(Integer.parseInt(instruction.getRd()),rs<<rt);
                PC+=4;
                break;
            case("BREAK"):
                PC+=4;
                break;
        }
        return PC;
    }

}
