public class Instruction {
    public String imm;
    public String Opcode;
    public String rs;
    public String rt;
    public String rd;
    public String Function;
    public String Immediate;
    public String Type;//指令类型
    public String base;

    public String getBase(){
        return base;
    }

    public String getRt(){
        return rt;
    }

    public void setRt(String rt){
        this.rt = rt;
    }

    public String getRs(){
        return rs;
    }

    public String getRd(){
        return rd;
    }

    public String getImmediate(){
        return Immediate;
    }

    public void setImmediate(String Immediate){
        this.Immediate = Immediate;
    }

    public String getFunction(){
        return Function;
    }

    public String getImm(){
        return imm;
    }

    public void setImm(String imm){
        this.imm = imm;
    }

    public String getOpcode(){
        return Opcode;
    }

    public void setOpcode(String opcode){
        this.Opcode = opcode;
    }

    public static String writeInByType(Instruction instruction,int address){
        String string = null;
        if(instruction.Type.equals("1")){
            string = "\t"+ String.valueOf(address) + "\t"+ instruction.Function +" R"+instruction.rd+", R"+instruction.rs+", R"+instruction.rt;
        }
        if(instruction.Type.equals("2")){
            string = "\t"+ String.valueOf(address) + "\t"+ instruction.Function +" R"+instruction.rt+", R"+instruction.rs+", #"+instruction.Immediate;
        }
        if (instruction.Type.equals("LSW")){
            string = "\t"+ String.valueOf(address) + "\t"+ instruction.Function +" R"+instruction.rt+ ", "+instruction.Immediate+"(R"+instruction.base+")";
        }
        if (instruction.Type.equals("BZ")){
            string = "\t"+ String.valueOf(address) + "\t"+ instruction.Function +" R"+instruction.rs+ ", #"+instruction.Immediate;
        }
        if (instruction.Type.equals("BEQ")){
            string = "\t"+ String.valueOf(address) + "\t"+ instruction.Function +" R"+instruction.rs+ ", R"+instruction.rt +", #"+instruction.Immediate;
        }
        if (instruction.Type.equals("J")){
            string = "\t"+ String.valueOf(address) + "\t"+ instruction.Function +" #"+instruction.Immediate;
        }
        if (instruction.Type.equals("JR")){
            string = "\t"+ String.valueOf(address) + "\t"+ instruction.Function +" R"+instruction.rs;
        }
        if (instruction.Type.equals("AL_SLL")){
            string = "\t"+ String.valueOf(address) + "\t"+ instruction.Function +" R"+instruction.rd+ ", R"+instruction.rt+ ", #"+instruction.Immediate;
        }
        if (instruction.Type.equals("BREAK")){
            string = "\t"+ String.valueOf(address) + "\t"+ instruction.Function;
        }
        if(instruction.Type.equals("NOP")){
            string = "\t"+ String.valueOf(address) + "\t"+ instruction.Function;
        }
        return string;
    }

    public static String writeInByType2(Instruction instruction,int address){
        String string=null;
        if(instruction.Type.equals("1")){
            string = "\t"+ String.valueOf(address) + "\t"+ instruction.Function +"\tR"+instruction.rd+", R"+instruction.rs+", R"+instruction.rt;
        }
        if(instruction.Type.equals("2")){
            string = "\t"+ String.valueOf(address) + "\t"+ instruction.Function +"\tR"+instruction.rt+", R"+instruction.rs+", #"+instruction.Immediate;
        }
        if (instruction.Type.equals("LSW")){
            string = "\t"+ String.valueOf(address) + "\t"+ instruction.Function +"\tR"+instruction.rt+ ", "+instruction.Immediate+"(R"+instruction.base+")";
        }
        if (instruction.Type.equals("BZ")){
            string = "\t"+ String.valueOf(address) + "\t"+ instruction.Function +"\tR"+instruction.rs+ ", #"+instruction.Immediate;
        }
        if (instruction.Type.equals("BEQ")){
            string = "\t"+ String.valueOf(address) + "\t"+ instruction.Function +"\tR"+instruction.rs+ ", R"+instruction.rt +", #"+instruction.Immediate;
        }
        if (instruction.Type.equals("J")){
            string = "\t"+ String.valueOf(address) + "\t"+ instruction.Function +"\t#"+instruction.Immediate;
        }
        if (instruction.Type.equals("JR")){
            string = "\t"+ String.valueOf(address) + "\t"+ instruction.Function +"\tR"+instruction.rs;
        }
        if (instruction.Type.equals("AL_SLL")){
            string = "\t"+ String.valueOf(address) + "\t"+ instruction.Function +"\tR"+instruction.rd+ ", R"+instruction.rt+ ", #"+instruction.Immediate;
        }
        if (instruction.Type.equals("BREAK")){
            string = "\t"+ String.valueOf(address) + "\t"+ instruction.Function;
        }
        if(instruction.Type.equals("NOP")){
            string = "\t"+ String.valueOf(address) + "\t"+ instruction.Function;
        }
        return string;
    }

    public static String toString(Instruction instruction){
        String string = null;
        if(instruction.Type.equals("1")){
            string = instruction.Function +"\tR"+instruction.rd+", R"+instruction.rs+", R"+instruction.rt;
        }
        if(instruction.Type.equals("2")){
            string = instruction.Function +"\tR"+instruction.rt+", R"+instruction.rs+", #"+instruction.Immediate;
        }
        if (instruction.Type.equals("LSW")){
            string = instruction.Function +" R"+instruction.rt+ ", "+instruction.Immediate+"(R"+instruction.base+")";
        }
        if (instruction.Type.equals("BZ")){
            string = instruction.Function +"\tR"+instruction.rs+ ", #"+instruction.Immediate;
        }
        if (instruction.Type.equals("BEQ")){
            string = instruction.Function +" R"+instruction.rs+ ", R"+instruction.rt +", #"+instruction.Immediate;
        }
        if (instruction.Type.equals("J")){
            string = instruction.Function +" #"+instruction.Immediate;
        }
        if (instruction.Type.equals("JR")){
            string = instruction.Function +" R"+instruction.rs;
        }
        if (instruction.Type.equals("AL_SLL")){
            string = instruction.Function +"\tR"+instruction.rd+ ", R"+instruction.rt+ ", #"+instruction.Immediate;
        }
        if (instruction.Type.equals("BREAK")){
            string = instruction.Function;
        }
        if(instruction.Type.equals("NOP")){
            string = instruction.Function;
        }
        return string;
    }

    public static String writeInAddress(String string,int address){
        return "\t" + String.valueOf(address)+"\t"+string;
    }

}
