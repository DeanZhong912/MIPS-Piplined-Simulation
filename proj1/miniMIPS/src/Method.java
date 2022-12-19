public class Method {
    // 二进制字符串转十进制
    public static int binaryToDecimal(String string){
        int count = 0;
        int temp = 1;
        for(int i = string.length();i>0;i--){
            count += (string.charAt(i-1)-'0')*temp;
            temp = temp*2;
        }
        return count;
    }

    public static String SLL(String string,int shift_num){
        int head = 0;
        StringBuilder stringBuilder = new StringBuilder(string);
        for(int i=shift_num;i<stringBuilder.length();i++){
            stringBuilder.setCharAt(head,stringBuilder.charAt(i));
            head++;
        }
        for (int i = stringBuilder.length()-shift_num;i<stringBuilder.length();i++){
            stringBuilder.setCharAt(i,'0');
        }
        string = stringBuilder.toString();
        return string;
    }
    
    // 二进制串转Instruction类
    public static Instruction transform(String string) {
        Instruction instruction = new Instruction();
        switch (string.substring(0, 6)) {
            case ("000000"):
                switch (string.substring(26)) {
                    case ("100000"):
                        instruction.Function = "ADD";
                        instruction.Type = "1";  //三个寄存器的输出类型为1
                        break;
                    case ("000000"):
                        if (string.substring(0).equals("00000000000000000000000000000000")) {
                            instruction.Function = "NOP";
                            instruction.Type = "NOP";
                        } else {
                            instruction.Function = "SLL";
                            instruction.Type = "AL_SLL";   //两个寄存器一个立即数的输出类型为2
                            instruction.Immediate = String.valueOf(binaryToDecimal(string.substring(21, 26)));
                        }
                        break;
                    case ("100010"):
                        instruction.Function = "SUB";
                        instruction.Type = "1";  //三个寄存器的输出类型为1
                        break;
                    case ("001101"):
                        instruction.Function = "BREAK";
                        instruction.Type = "BREAK";     //break的输出类型
                        break;
                    case ("000011"):
                        instruction.Function = "SRA";
                        instruction.Type = "AL_SLL";
                        break;
                    case ("000010"):
                        instruction.Function = "SRL";
                        instruction.Type = "AL_SLL";
                        break;
                    case ("001000"):
                        instruction.Function = "JR";
                        instruction.Type = "JR";  //J的输出类
                        break;
                    default:
                        break;
                }
                break;
            case ("100011"):
                instruction.Function = "LW";
                instruction.Type = "LSW";  //LW和SW的专属类型，一个寄存器，一个立即数，并标明立即数的位数
                break;
            case ("000100"):
                instruction.Function = "BEQ";
                instruction.Type = "BEQ";  //两个寄存器一个立即数的输出类型为2
                break;
            case ("000010"):
                instruction.Function = "J";
                instruction.Type = "J";  //J的输出类型
                break;
            case ("101011"):
                instruction.Function = "SW";
                instruction.Type = "LSW";  //LW和SW的专属类型，一个寄存器，一个立即数，并标明立即数的位数
                break;
            case ("000111"):
                instruction.Function = "BGTZ";
                instruction.Type = "BZ";  //BGTZ和BLTZ的专属类型，一个寄存器一个立即数
                break;
            case ("000001"):
                instruction.Function = "BLTZ";
                instruction.Type = "BZ";  //一个寄存器一个offset的输出类型为3
                break;
            case ("011100"):
                instruction.Function = "MUL";
                instruction.Type = "1";  //三个寄存器的输出类型为1
                break;
            default:
                if (string.substring(0, 1).equals("1")) {    //category2类型的命令，最后16位为立即数
                    instruction.imm = "1";
                    switch (string.substring(1, 6)) {
                        case ("10000"):
                            instruction.Function = "ADD";
                            instruction.Type = "2";  //两个寄存器一个立即数的输出类型为2
                            break;
                        case ("10001"):
                            instruction.Function = "SUB";
                            instruction.Type = "2";
                            break;
                        case ("00001"):
                            instruction.Function = "MUL";
                            instruction.Type = "2";
                            break;
                        case ("10010"):
                            instruction.Function = "AND";
                            instruction.Type = "2";
                            break;
                        case ("10011"):
                            instruction.Function = "NOR";
                            instruction.Type = "2";
                            break;
                        case ("10101"):
                            instruction.Function = "SLT";
                            instruction.Type = "2";
                            break;
                        default:
                            break;
                    }
                } else {
                    instruction.imm = "0";                      //category2类型的命令，三个寄存器
                    switch (string.substring(1, 6)) {
                        case ("10000"):
                            instruction.Function = "ADD";
                            instruction.Type = "1";
                            break;
                        case ("10001"):
                            instruction.Function = "SUB";
                            instruction.Type = "1";
                            break;
                        case ("00001"):
                            instruction.Function = "MUL";
                            instruction.Type = "1";
                            break;
                        case ("10010"):
                            instruction.Function = "AND";
                            instruction.Type = "1";
                            break;
                        case ("10011"):
                            instruction.Function = "NOR";
                            instruction.Type = "1";
                            break;
                        case ("10101"):
                            instruction.Function = "SLT";
                            instruction.Type = "1";
                            break;
                        default:
                            break;
                    }
                    //三个寄存器的输出类型为1
                }
                break;
        }
        if(instruction.Type.equals("1")){
            instruction.rs = String.valueOf(binaryToDecimal(string.substring(6,11)));
            instruction.rt = String.valueOf(binaryToDecimal(string.substring(11,16)));
            instruction.rd = String.valueOf(binaryToDecimal(string.substring(16,21)));
        }
        if(instruction.Type.equals("2")){
            instruction.rs = String.valueOf(binaryToDecimal(string.substring(6,11)));
            instruction.rt = String.valueOf(binaryToDecimal(string.substring(11,16)));
            instruction.setImmediate(String.valueOf(binaryToDecimal(string.substring(16))));
        }
        if(instruction.Type.equals("LSW")){
            instruction.base = String.valueOf(binaryToDecimal(string.substring(6,11)));
            instruction.rt = String.valueOf(binaryToDecimal(string.substring(11,16)));
            instruction.setImmediate(String.valueOf(binaryToDecimal(string.substring(16))));
        }
        if(instruction.Type.equals("BZ")){
            instruction.rs = String.valueOf(binaryToDecimal(string.substring(6,11)));
            instruction.setImmediate(String.valueOf(binaryToDecimal(SLL(string.substring(16),2))));
        }
        if(instruction.Type.equals("J")){
            instruction.rs = String.valueOf(binaryToDecimal(string.substring(6,11)));
            instruction.setImmediate(String.valueOf(binaryToDecimal(SLL(string.substring(16),2))));
        }
        if(instruction.Type.equals("JR")){
            instruction.rs = String.valueOf(binaryToDecimal(string.substring(6,11)));
        }
        if(instruction.Type.equals("AL_SLL")){
            instruction.rt = String.valueOf(binaryToDecimal(string.substring(11,16)));
            instruction.rd = String.valueOf(binaryToDecimal(string.substring(16,21)));
            instruction.setImmediate(String.valueOf(binaryToDecimal(string.substring(21,26))));
        }
        if(instruction.Type.equals("BEQ")) {
            instruction.rs = String.valueOf(binaryToDecimal(string.substring(6, 11)));
            instruction.rt = String.valueOf(binaryToDecimal(string.substring(11, 16)));
            instruction.setImmediate(String.valueOf(binaryToDecimal(SLL(string.substring(16),2))));
        }
        return instruction;
    }

    public static int binaryComplementToDecimal(String code){
        //如果是1开头，需要转补码，0则不需要
        if(code.charAt(0)=='1'){
            StringBuilder stringBuilder = new StringBuilder(code);
            int i=stringBuilder.length()-1;
            while(i>=1){
                if (stringBuilder.charAt(i)=='1'){
                    stringBuilder.setCharAt(i,'0');
                }
                else stringBuilder.setCharAt(i,'1');
                i--;
            }
            for(i=stringBuilder.length()-1;i>=1&&(stringBuilder.charAt(i)=='1');i--);
            stringBuilder.setCharAt(i,'1');
            i++;
            while(i<stringBuilder.length()) {
                stringBuilder.setCharAt(i, '0');
                i++;
            }
            code = stringBuilder.toString();

            return -binaryToDecimal(code.substring(1,code.length()));
        }
        return binaryToDecimal(code);
    }

    public static String ComplementToBinary(int num){
        String string;
        int need_length;
        if(num<0)
            string = Integer.toBinaryString(num);
        else {
            string = Integer.toBinaryString(num);
            need_length = 32-string.length();
            for(int i=0;i<need_length;i++)
                string = "0"+string;
        }
        return string;
    }
}
