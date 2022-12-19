import java.io.*;

public class Main {
    public static void main(String[] args) throws IOException {
        FileReader input = new FileReader("sample.txt");
        String tempString,originalString,finalString;
        Instruction instruction;
        Memory memory = new Memory();
        // 写入文件
        FileWriter fileWriter = new FileWriter("Mydisassembly.txt");
        PrintWriter printWriter1 = new PrintWriter(fileWriter);
        FileWriter fileWriter2 = new FileWriter("Mysimulation.txt");
        PrintWriter printWriter2 = new PrintWriter(fileWriter2);

        int base_address = 64, address = 64,flag=0,break_position=0;
        BufferedReader bufferedReader = new BufferedReader(input);
        while ((tempString=bufferedReader.readLine())!=null) {//循环读取输入中的二进制码
            tempString = tempString.substring(0,32);
            originalString = tempString;
            if(flag==0){
                instruction = Method.transform(tempString);//二进制码转命令
                if(instruction.Function.equals("BREAK")){
                    flag = 1;
                    break_position = address;
                }
                tempString = Instruction.writeInByType(instruction,address);
                StringBuilder stringBuilder = new StringBuilder(originalString);
                stringBuilder.insert(6,' ');
                stringBuilder.insert(12,' ');
                stringBuilder.insert(18,' ');
                stringBuilder.insert(24,' ');
                stringBuilder.insert(30,' ');
                finalString = stringBuilder.toString();
                printWriter1.println(finalString+tempString);
                System.out.println(finalString+tempString);
            }else{
                tempString = String.valueOf(Method.binaryComplementToDecimal(tempString));
                tempString = Instruction.writeInAddress(tempString,address);
                printWriter1.println(originalString+tempString);
                System.out.println(originalString+tempString);
            }
            address = address+4;
            memory.pushVector(originalString);
        }
        printWriter1.flush();
        printWriter1.close();

        MIPS_sim cpu = new MIPS_sim(memory,break_position);
        cpu.Simulation(printWriter2);
        printWriter2.flush();
        printWriter2.close();
    }
}
