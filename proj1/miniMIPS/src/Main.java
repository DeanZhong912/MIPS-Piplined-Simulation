import java.io.*;

public class Main {
    public static void main(String[] args) throws IOException {
        Instruction instruction = new Instruction();
        Memory memory = new Memory();
        int base_address = 64, address = 64,flag=0,break_position=0;
        String tempString,originalString,finalString;
        FileReader input = new FileReader("sample.txt");
        BufferedReader bufferedReader = new BufferedReader(input);
        FileWriter output1 = new FileWriter("disassembly.txt");
        FileWriter output2 = new FileWriter("simulation.txt");
        PrintWriter printWriter1 = new PrintWriter(output1); // 写入disassembly文件
        PrintWriter printWriter2 = new PrintWriter(output2); // 写入simulation文件
        while ((tempString=bufferedReader.readLine())!=null) {//循环读取输入中的二进制码
            //System.out.println(tempString);
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

        // simulation
        CPU cpu = new CPU(memory,break_position);
        cpu.calculation(printWriter2);
        printWriter2.flush();
        printWriter2.close();

    }
}
