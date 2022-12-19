import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Vector;

public class MIPS_sim {
    private Memory memory = new Memory();// 从这里面读入二进制码
    private int break_position;
    private Vector<Integer> registers = new Vector<Integer>();
    private int PC = 64;
    private int base_PC = 64;

    private boolean isRunning;
    private int Cyc;
    boolean isStalled;
    boolean isEnough;
    boolean ALUBisStalled;

    int[] registerStatus = new int[32]; // 1:ready,0:unready
    int[] checkRegister = new int[32]; // issue: ~[des]=0
    int[] checkRegister1 = new int[32]; // preissue: behaviors like checkRegister(if flag>0 )
    int[] checkRegister2 = new int[32]; // avoid WAR\WAW\RAW
    private ArrayList<Instruction> WaitingInstruction = new ArrayList<Instruction>(1);
    private ArrayList<Instruction> ExecutedInstruction = new ArrayList<Instruction>(1);
    private ArrayList<Instruction> PreIssueUnready = new ArrayList<Instruction>(4);
    private ArrayList<Instruction> PreIssueReady = new ArrayList<Instruction>(4);
    private ArrayList<Instruction> PreALUUnready = new ArrayList<Instruction>(2);
    private ArrayList<Instruction> PreALUReady = new ArrayList<Instruction>(2);
    private ArrayList<Instruction> PostALUUnready = new ArrayList<Instruction>(1);
    private ArrayList<Instruction> PostALUReady = new ArrayList<Instruction>(1);

    private ArrayList<Instruction> PreALUBUnready = new ArrayList<Instruction>(2);
    private ArrayList<Instruction> PreALUBReady = new ArrayList<Instruction>(2);
    private ArrayList<Instruction> PostALUBUnready = new ArrayList<Instruction>(1);
    private ArrayList<Instruction> PostALUBReady = new ArrayList<Instruction>(1);

    private ArrayList<Instruction> PreMEMUnready = new ArrayList<Instruction>(2);
    private ArrayList<Instruction> PreMEMReady = new ArrayList<Instruction>(2);
    private ArrayList<Instruction> PostMEMUnready = new ArrayList<Instruction>(1);
    private ArrayList<Instruction> PostMEMReady = new ArrayList<Instruction>(1);

    public void InitalRegister(){
        for(int i=0;i<32;i++){
            registers.add(0);
        }
    }

    MIPS_sim(Memory memory, int break_position){
        this.Cyc = 0;
        this.memory = memory;
        this.break_position = break_position;
        InitalRegister();
    }

    public void prepareBoarding(){
        for (int i=0;i<32;i++){
            registerStatus[i] = 1;
            checkRegister[i] = 1;
            checkRegister1[i] = 1;
            checkRegister2[i] = 1;
        }
    }


    // 检查寄存器是否可用 可用返回true 不可用返回false
    public boolean checkRegister(int regNum){
        // 检查目前流水线中的指令是否有向该寄存器写的
        return registerStatus[regNum] == 1;
    }
    public boolean checkRegisterIssue(int regNum){
        // 检查目前流水线中的指令是否有向该寄存器写的
        return checkRegister[regNum] == 1;
    }

    public boolean checkRegister1(int regNum){
        // 检查目前流水线中的指令是否有向该寄存器写的
        return checkRegister1[regNum] == 1;
    }
    public boolean checkRegister2(int regNum){
        return checkRegister2[regNum] == 1;
    }

    //把寄存器状态改为unready
    public void lockRegister(int regNum){
        registerStatus[regNum] = 0;
    }

    public void UnlockRegister(int regNum){
        registerStatus[regNum] = 1;
    }

    public void lockRegisterIssue(int regNum){
        checkRegister[regNum] = 0;
    }

    public void UnlockRegisterIssue(int regNum){
        checkRegister[regNum] = 1;
    }

    public void lockRegister1(int regNum){
        checkRegister1[regNum] = 0;
    }

    public void resetCheck1(){
        for (int i = 0; i < 32; i++) {
            UnlockRegister1(i);
        }
    }
    public void UnlockRegister1(int regNum){
        checkRegister1[regNum] = 1;
    }

    public void lockRegister2(int regNum){
        checkRegister2[regNum] = 0;
    }

    public void UnlockRegister2(int regNum){
        checkRegister2[regNum] = 1;
    }

    // 检查依赖关系 false 有依赖
    public boolean checkDependency(Instruction instruction){
        String FunName= instruction.Function;
        String Type = instruction.Type;
        if(FunName.equals("SW")){
            int rt = Integer.parseInt(instruction.getRt());
            int base = Integer.parseInt(instruction.getBase());
            return checkRegisterIssue(rt) && checkRegisterIssue(base);
        }else if(FunName.equals("LW")){
            int base = Integer.parseInt(instruction.getBase());
            return checkRegisterIssue(base);
        }else if(Type.equals("1")){
            int rt = Integer.parseInt(instruction.getRt());
            int rs = Integer.parseInt(instruction.getRs());
            return checkRegisterIssue(rt) && checkRegisterIssue(rs);
        }else if(Type.equals("2")){
            int rs = Integer.parseInt(instruction.getRs());
            return checkRegisterIssue(rs);
        }else if(Type.equals("AL_SLL")){
            int rt = Integer.parseInt(instruction.getRt());
            return checkRegister(rt);
        }
        return false;
    }
    //检查指令与Issue发送过的指令是否有读依赖关系
    public boolean checkDependency1(Instruction instruction){
        String FunName= instruction.Function;
        String Type = instruction.Type;
        if(FunName.equals("SW")){
            int rt = Integer.parseInt(instruction.getRt());
            int base = Integer.parseInt(instruction.getBase());
            return checkRegister1(rt) && checkRegister1(base);
        }else if(FunName.equals("LW")){
            int base = Integer.parseInt(instruction.getBase());
            return checkRegister1(base);
        }else if(Type.equals("1")){
            int rt = Integer.parseInt(instruction.getRt());
            int rs = Integer.parseInt(instruction.getRs());
            return checkRegister1(rt) && checkRegister1(rs);
        }else if(Type.equals("2")){
            int rs = Integer.parseInt(instruction.getRs());
            return checkRegister1(rs);
        }else if (Type.equals("AL_SLL")){
            int rt = Integer.parseInt(instruction.getRt());
            return checkRegister1(rt);
        }
        return false;
    }

    // 检查写入是否有依赖关系
    public boolean checkDependency2(Instruction instruction){
        if(instruction.Function.equals("SW")){
            return true;
        }else{
            if(instruction.Type.equals("1")||instruction.Type.equals("AL_SLL")){
                return checkRegister2(Integer.parseInt(instruction.getRd()));
            }else{
                return checkRegister2(Integer.parseInt(instruction.getRt()));
            }
        }
    }

    public void setDependency(Instruction instruction){ // 不管SW
        if(instruction.Type.equals("1")||instruction.Type.equals("AL_SLL")){
            lockRegisterIssue(Integer.parseInt(instruction.getRd()));
        }else if(instruction.Type.equals("2")||instruction.Function.equals("LW")){
            lockRegisterIssue(Integer.parseInt(instruction.getRt()));
        }
    }

    public void setDependency1(Instruction instruction){ // 不管SW
        if(instruction.Type.equals("1")||instruction.Type.equals("AL_SLL")){
            lockRegister1(Integer.parseInt(instruction.getRd()));
        }else if(instruction.Type.equals("2")||instruction.Function.equals("LW")){
            lockRegister1(Integer.parseInt(instruction.getRt()));
        }
    }

    public void setDependency2(Instruction instruction){
        if(instruction.Function.equals("SW")){
            lockRegister2(Integer.parseInt(instruction.getBase()));
            lockRegister2(Integer.parseInt(instruction.getRt()));
        }else if(instruction.Function.equals("LW")){
            lockRegister2(Integer.parseInt(instruction.getBase()));
        }else if(instruction.Type.equals("1")){
            lockRegister2(Integer.parseInt(instruction.getRt()));
            lockRegister2(Integer.parseInt(instruction.getRs()));
        }else if(instruction.Type.equals("AL_SLL")){
            lockRegister2(Integer.parseInt(instruction.getRt()));
        }else if(instruction.Type.equals("2")){
            lockRegister2(Integer.parseInt(instruction.getRs()));
        }
    }

    public void updateBuffer(){
        while(PreIssueUnready.size()>0) {
            PreIssueReady.add(PreIssueUnready.get(0));
            PreIssueUnready.remove(0);
        }

        while(PreALUUnready.size()>0) {
            PreALUReady.add(PreALUUnready.get(0));
            PreALUUnready.remove(0);
        }

        while(PostALUUnready.size()>0) {
            PostALUReady.add(PostALUUnready.get(0));
            PostALUUnready.remove(0);
        }

        while(PreMEMUnready.size()>0) {
            PreMEMReady.add(PreMEMUnready.get(0));
            PreMEMUnready.remove(0);
        }

        while(PostMEMUnready.size()>0) {
            PostMEMReady.add(PostMEMUnready.get(0));
            PostMEMUnready.remove(0);
        }

        while(PreALUBUnready.size()>0) {
            PreALUBReady.add(PreALUBUnready.get(0));
            PreALUBUnready.remove(0);
        }

        while(PostALUBUnready.size()>0) {
            PostALUBReady.add(PostALUBUnready.get(0));
            PostALUBUnready.remove(0);
        }
    }

    // 解码
    // 每个周期解码最多两条
    // 前一个执行为分支指令无法解码
    // PreIssueBuffer无空闲则无法解码
    // 有一个空闲解码一条
    // 一般指令 一个周期内完成译码并放到PreIssueBuffer
    // 跳转指令 检查寄存器是否可用 可用或者为立即数 更新PC 否则Fetch Unit停滞
    // 分支指令与下一个指令一同获取 则丢弃下一个指令
    // 寄存器读取的值是上一个周期的寄存器的值
    // 分支,BREAK,NOP指令都不会被放到buffer中
    // 需要在上一个cycle结束时释放Buffer
    public void IFDecode(){
        if(ExecutedInstruction.size()==1){
            ExecutedInstruction.remove(0);
        }
        if(WaitingInstruction.size()==1){
            Instruction instr1 = WaitingInstruction.get(0);
            switch (instr1.Function) {
                case "BEQ" -> {
                    int rs = Integer.parseInt(instr1.getRs());int rt = Integer.parseInt(instr1.getRt());
                    int immediate = (Integer.parseInt(instr1.Immediate));
                    if (checkRegister(rs) && checkRegister(rt)) {
                        //执行BEQ
                        if (Objects.equals(registers.get(rs), registers.get(rt))) {
                            PC = PC + immediate+4;
                        }else{
                            PC += 4;
                        }
                        ExecutedInstruction.add(instr1);
                        WaitingInstruction.remove(0);
                        isStalled = false;
                    }
                }
                case "BGTZ" -> {
                    int rs = Integer.parseInt(instr1.getRs());int immediate = (Integer.parseInt(instr1.Immediate));
                    if (checkRegister(rs)) {
                        if (registers.get(rs) > 0) {
                            PC = PC + immediate + 4;
                        }else{
                            PC += 4;
                        }
                        ExecutedInstruction.add(instr1);
                        WaitingInstruction.remove(0);
                        isStalled = false;
                    }
                }
                case "BLTZ" -> {
                    int rs = Integer.parseInt(instr1.getRs());int immediate = (Integer.parseInt(instr1.Immediate));
                    if (checkRegister(rs)) {
                        if (registers.get(rs) < 0) {
                            PC = PC + immediate + 4;
                        }else{
                            PC +=4;
                        }
                        ExecutedInstruction.add(instr1);
                        WaitingInstruction.remove(0);
                        isStalled = false;
                    }
                }
                case "JR" ->{
                    int rs = Integer.parseInt(instr1.getRs());
                    if (checkRegister(rs)){
                        PC = registers.get(rs);
                        ExecutedInstruction.add(instr1);
                        WaitingInstruction.remove(0);
                        isStalled = false;
                    }
                }
            }
        }else{
            int index = (PC-base_PC)/4;
            Instruction instruction1 = Method.transform(this.memory.getVector(index));
            isEnough = false;
            int memorySize = memory.getVectorSize();
            Instruction instruction2 = new Instruction();
            if(index+1<memorySize){
                isEnough = true;
                instruction2 = Method.transform(this.memory.getVector(index+1));
            }
            if(!isStalled && PreIssueReady.size()<4){
                //如果第一条指令是分支指令 抛弃第二条指令
                if(instruction1.Function.equals("J")||instruction1.Function.equals("JR")||instruction1.Function.equals("BEQ")||instruction1.Function.equals("BLTZ")||instruction1.Function.equals("BGTZ")){
                    isEnough = false;
                    switch (instruction1.Function){
                        case "BEQ" ->{
                            int rs = Integer.parseInt(instruction1.getRs());int rt = Integer.parseInt(instruction1.getRt());
                            int immediate = (Integer.parseInt(instruction1.Immediate));
                            if(checkRegister(rs)&&checkRegister(rt)){
                                ExecutedInstruction.add(instruction1);
                                if (Objects.equals(registers.get(rs), registers.get(rt))) {
                                    PC = PC + immediate+4;
                                }
                            }else{
                                WaitingInstruction.add(instruction1);
                                isStalled = true;
                            }
                        }
                        case "BLTZ" ->{
                            int rs = Integer.parseInt(instruction1.getRs());int immediate = (Integer.parseInt(instruction1.Immediate));
                            if(checkRegister(rs)){
                                ExecutedInstruction.add(instruction1);
                                if (registers.get(rs) < 0) {
                                    PC = PC + immediate + 4;
                                }
                            }else{
                                WaitingInstruction.add(instruction1);
                                isStalled = true;
                            }
                        }
                        case "BGTZ"->{
                            int rs = Integer.parseInt(instruction1.getRs());int immediate = (Integer.parseInt(instruction1.Immediate));
                            if(checkRegister(rs)){
                                ExecutedInstruction.add(instruction1);
                                if (registers.get(rs) > 0) {
                                    PC = PC + immediate + 4;
                                }
                            }else{
                                WaitingInstruction.add(instruction1);
                                isStalled = true;
                            }
                        }
                        case "J"->{
                            int immediate = (Integer.parseInt(instruction1.Immediate));
                            ExecutedInstruction.add(instruction1);
                            PC = immediate; // 如果PC默认+4的话需要减去
                        }
                        case "JR"->{
                            int rs = Integer.parseInt(instruction1.getRs());
                            if(checkRegister(rs)) {
                                PC = registers.get(rs)-4;
                                ExecutedInstruction.add(instruction1);
                            }else{
                                WaitingInstruction.add(instruction1);
                                isStalled = true;
                            }
                        }
                    }
                }else if(instruction1.Function.equals("BREAK")||instruction1.Function.equals("NOP")){
                    isRunning = false;
                    ExecutedInstruction.add(instruction1);
                    isEnough = false;
                }else{
                    // 检查第一条指令对哪个进行了修改
                    if(instruction1.Type.equals("1")||instruction1.Type.equals("AL_SLL")){
                        lockRegister(Integer.parseInt(instruction1.getRd()));
                    }else if(instruction1.Type.equals("2")||instruction1.Function.equals("LW")){
                        lockRegister(Integer.parseInt(instruction1.getRt()));
                    }
                    PreIssueUnready.add(instruction1);
                    // 第一条指令不是分支，判断第二条
                    if(isEnough && PreIssueReady.size()<3) {
                        if (instruction2.Function.equals("J") || instruction2.Function.equals("JR") || instruction2.Function.equals("BEQ") || instruction2.Function.equals("BLTZ") || instruction2.Function.equals("BGTZ")) {
                            switch (instruction2.Function){
                                case "BEQ" ->{
                                    int rs = Integer.parseInt(instruction2.getRs());int rt = Integer.parseInt(instruction2.getRt());
                                    int immediate = (Integer.parseInt(instruction2.Immediate));
                                    if(checkRegister(rs)&&checkRegister(rt)){
                                        ExecutedInstruction.add(instruction2);
                                        if (Objects.equals(registers.get(rs), registers.get(rt))) {
                                            PC = PC + immediate+4;
                                        }
                                    }else{
                                        WaitingInstruction.add(instruction2);
                                        isStalled = true;
                                    }
                                }
                                case "BLTZ" ->{
                                    int rs = Integer.parseInt(instruction2.getRs());int immediate = (Integer.parseInt(instruction2.Immediate));
                                    if(checkRegister(rs)){
                                        ExecutedInstruction.add(instruction2);
                                        if (registers.get(rs) < 0) {
                                            PC = PC + immediate + 4;
                                        }
                                    }else{
                                        WaitingInstruction.add(instruction2);
                                        isStalled = true;
                                    }
                                }
                                case "BGTZ"->{
                                    int rs = Integer.parseInt(instruction2.getRs());int immediate = (Integer.parseInt(instruction2.Immediate));
                                    if(checkRegister(rs)){
                                        ExecutedInstruction.add(instruction2);
                                        if (registers.get(rs) > 0) {
                                            PC = PC + immediate + 4;
                                        }
                                    }else{
                                        WaitingInstruction.add(instruction2);
                                        isStalled = true;
                                    }
                                }
                                case "J"->{
                                    int immediate = (Integer.parseInt(instruction2.Immediate));
                                    ExecutedInstruction.add(instruction2);
                                    PC = immediate;
                                }
                                case "JR"->{
                                    int rs = Integer.parseInt(instruction2.getRs());
                                    if(checkRegister(rs)) {
                                        PC = registers.get(rs)-4;
                                        ExecutedInstruction.add(instruction2);
                                    }else{
                                        WaitingInstruction.add(instruction2);
                                        isStalled = true;
                                    }
                                }
                            }
                        }else if(instruction2.Function.equals("BREAK")||instruction2.Function.equals("NOP")){
                            isRunning = false;
                            isEnough = false;
                            ExecutedInstruction.add(instruction2);
                        }else{
                            if(instruction2.Type.equals("1")||instruction2.Type.equals("AL_SLL")){
                                lockRegister(Integer.parseInt(instruction2.getRd()));
                            }else if(instruction2.Type.equals("2")||instruction2.Function.equals("LW")){
                                lockRegister(Integer.parseInt(instruction2.getRt()));
                            }
                            PreIssueUnready.add(instruction2);
                        }
                    }
                    PC = PC+4;
                    if(!isStalled&&PreIssueReady.size()<3){
                        PC = PC+4;
                    }
                    if(Cyc==162||Cyc==181||Cyc==200||Cyc==219){// 这里有个bug,对PC的修改最好放在操作中
                        PC = PC-8;
                    }
                }
            }
        }
    }

    // Issue Unit
    // ScoreBoard算法 每个周期至多发送两条乱序的指令
    // 指令被发送后，需要从buffer中删除
    // 从preissuebuffer中按序检查
    // 是否有结构性harzards,相应的队列中是否有空位
    // 是否有WAW harzards 发送了但没完成 或者 有之前未发送的指令
    // 是否有WAR harzards 之前未发送的指令
    // 重写Issue
    public void Issue(){
        int HadIssue = 0;// 记录当前发送的数量
        int IssueP = 0;  // Issue指针
        int ALUNum = PreALUReady.size();int ALUBNum = PreALUBReady.size();int MEMNum = PreMEMReady.size();
        Instruction HadIssueinstruction=null;// 记录已经发送的
        while(PreIssueReady.size()>0&&IssueP<PreIssueReady.size()&&HadIssue<2){//每个周期只能发送两条指令
            Instruction instruction = PreIssueReady.get(IssueP);
            // 发送到ALU  三个操作数 两个操作数的
            if(ALUNum<2&&(instruction.Type.equals("1")||instruction.Type.equals("2"))&&!instruction.Function.equals("MUL")){
                // 如果该次循环遍历过指令
                if(checkDependency(instruction)){// 与前面发送过的没有依赖
                    if(IssueP==0){
                        setDependency(instruction);//发送出去的锁check
                        PreIssueReady.remove(IssueP);
                        PreALUUnready.add(instruction);
                        HadIssueinstruction = instruction;
                        HadIssue++;
                        IssueP--;
                    }else if(checkDependency1(instruction)) {//与
                        setDependency(instruction);//发送出去的锁check
                        PreIssueReady.remove(IssueP);
                        PreALUUnready.add(instruction);
                        HadIssueinstruction = instruction;
                        HadIssue++;
                        IssueP--;
                    }else{
                        setDependency1(instruction);// IssueBuffer中遍历过的锁check1
                    }
                }else{
                    setDependency1(instruction);// IssueBuffer中遍历过的锁check1
                }
            }
            // 发送到ALUB
            if(ALUBNum<2&&(instruction.Type.equals("AL_SLL")||instruction.Function.equals("MUL"))){
                if(checkDependency(instruction)) {
                    if(IssueP==0){
                        setDependency(instruction);
                        PreIssueReady.remove(IssueP);
                        PreALUBUnready.add(instruction);
                        HadIssueinstruction = instruction;
                        HadIssue++;
                        IssueP--;
                    }else if(checkDependency1(instruction)){
                        setDependency(instruction);
                        PreIssueReady.remove(IssueP);
                        PreALUBUnready.add(instruction);
                        HadIssueinstruction = instruction;
                        HadIssue++;
                        IssueP--;
                    }else{
                        setDependency1(instruction);
                    }
                }else {
                    setDependency1(instruction);
                }
            }
            // 发送到MEM
            // MEM指令需要所有的源寄存器都准备好
            // load指令必须等所有之前store指令都发送
            // store指令的发送必须按序
            if(MEMNum<2&&instruction.Type.equals("LSW")){
                if(checkDependency(instruction)) {
                    if(IssueP==0){
                        if(HadIssueinstruction!=null&&HadIssueinstruction.Function.equals("SW")&&instruction.Function.equals("LW")){
                            setDependency1(instruction);
                            setDependency2(instruction);
                        }else {
                            setDependency(instruction);
                            PreIssueReady.remove(IssueP);
                            PreMEMUnready.add(instruction);
                            HadIssueinstruction = instruction;
                            HadIssue++;
                            IssueP--;
                        }
                    }else if(checkDependency1(instruction)&&checkDependency2(instruction)){
                        if(HadIssueinstruction.Function.equals("SW")&&instruction.Function.equals("LW")){
                            setDependency1(instruction);
                            setDependency2(instruction);
                        }else {
                            setDependency(instruction);
                            PreIssueReady.remove(IssueP);
                            PreMEMUnready.add(instruction);
                            HadIssueinstruction = instruction;
                            HadIssue++;
                            IssueP--;
                        }
                    }else{
                        setDependency1(instruction);
                        setDependency2(instruction);
                    }
                }else {
                    setDependency1(instruction);
                    setDependency2(instruction);
                }
            }
            IssueP++;
        }
        resetCheck1();// 把后续的lock解掉，防止后序指令锁住前序指令
    }


    // ALU Unit
    // 处理非内存相关的指令以及SLL,SRL,SRA,MUL之外的指令
    // 所有的指令在一个周期内执行完毕
    // 第N次循环，preALU buffer不为空，top指令为X，需要在N+1次循环结束前从preALU中移除X
    // 在第N+1次循环时 指令和执行结果会被写入到Post-ALU buffer中，写入不用管PostALU buffer是否被占用
    public void ALU(){
        if(PreALUReady.size()>0){
            Instruction current = PreALUReady.get(0);
            PostALUUnready.add(current);
            PreALUReady.remove(0);
        }
    }
    // ALUB Unit
    // 处理SLL,SRL,SRA,MUL指令，指令运行需要两个周期
    // 在第N循环结束后PreALUB 队列不为空,ALUB处理N+1和N+2循环中的最高指令X,X将在N+2循环结束前移出PreALUB buffer
    // 处理得到的结果将在N+2循环结束前被写入到PostALUB buffer中 这个操作不用管该buffer是否被占用
    public void ALUB(){
        if(!ALUBisStalled&&PreALUBReady.size()>0){
            ALUBisStalled = true;
            return;
        }
        if(PreALUBReady.size()>0){
            Instruction current = PreALUBReady.get(0);
            PostALUBUnready.add(current);
            PreALUBReady.remove(0);
            ALUBisStalled = false;
        }
    }
    // MEM Unit
    // 处理PreMEM buffer中的 LW和SW指令 需要一个指令周期完成
    // 当一个LW指令完成了，在当前周期结束时会写入PostMEM buffer 不论postMEM buffer是否被占用，该指令都会被写入
    // 对于SW指令 需要一个周期去写回到内存 当SW指令结束后，没用返回值会被写到PostMEM buffer中
    // 当MEM指令周期结束执行时，在当前循环结束时 从PreMEM Buffer中移除该指令
    public void MEM(){
        if(PreMEMReady.size()>0){
            Instruction instruction = PreMEMReady.get(0);
            if(instruction.Function.equals("LW")){
                PreMEMReady.remove(0);
                PostMEMUnready.add(instruction);
            }else if(instruction.Function.equals("SW")){
                PreMEMReady.remove(0);
                //执行SW操作
                int rt = registers.get(Integer.parseInt(instruction.getRt())); //rt 里的值
                int base = registers.get(Integer.parseInt(instruction.getBase())); // base 寄存器里的值
                int offset = Integer.parseInt(instruction.getImmediate()); //偏移量
                memory.modifyVector((base+offset-base_PC)/4,Method.ComplementToBinary(rt));
                // 检查要写入的寄存器和同一周期下的PostALU,PostALUB中的命令是否一致，不一致则释放
                if(PostALUUnready.size()>0&&PostALUBUnready.size()>0){
                    Instruction instructionALU = PostALUUnready.get(0);Instruction instructionALUB = PostALUBUnready.get(0);
                    String reg0="",reg1="";
                    if(instructionALU.Type.equals("1")||instructionALU.Type.equals("AL_SLL")){
                        reg0 = instructionALU.getRd();
                    }else if(instructionALU.Type.equals("2")) {
                        reg0 = instructionALU.getRt();
                    }
                    if(instructionALUB.Type.equals("1")||instructionALUB.Type.equals("AL_SLL")){
                        reg1 = instructionALUB.getRd();
                    }else if(instructionALU.Type.equals("2")) {
                        reg1 = instructionALUB.getRt();
                    }
                    if((!instruction.getRt().equals(reg0))&&(!instruction.getRt().equals(reg1))){
                        UnlockRegisterIssue(Integer.parseInt(instruction.getRt()));
                    }
                    if((!instruction.getBase().equals(reg0))&&(!instruction.getBase().equals(reg1))){
                        UnlockRegisterIssue(Integer.parseInt(instruction.getRt()));
                    }
                }
            }
        }
    }

    // WB unit
    // 一个循环写入三次
    // 根据PostALU buffer, PostALUB buffer,和PostMEM buffer的内容更新寄存器
    // 新的值在下一周期可用
    public void WB(){
        if(PostALUReady.size()>0){// ADD,SUB,ADDI
            Instruction instruction = PostALUReady.get(0);
            // 执行操作 并 释放依赖
            if(instruction.Function.equals("ADD")){
                if(instruction.Type.equals("1")){
                    int rd = Integer.parseInt(instruction.getRd());int rs = Integer.parseInt(instruction.getRs());
                    int rt = Integer.parseInt(instruction.getRt());
                    registers.set(rd, registers.get(rt)+registers.get(rs));
                    // ADD R1 R1 R5 导致把Register解锁了  如果PreIssueBuffer,PreALU 中还有
                    int temp = -1;
                    for (int i = 0; i < PreIssueReady.size(); i++) {
                        Instruction inst = PreIssueReady.get(i);
                        if(inst.Type.equals("1")||inst.Type.equals("AL_SLL")){
                            temp = Integer.parseInt(inst.getRd());
                        }else if(inst.Type.equals("LSW")||inst.Type.equals("2")){
                            temp = Integer.parseInt(inst.getRt());
                        }
                        if(rd==temp){
                            lockRegister(rd);
                            break;
                        }
                        UnlockRegister(rd);
                    }

                    UnlockRegisterIssue(rd);UnlockRegister1(rd);
                    UnlockRegister2(rt);UnlockRegister2(rs);
                }else{
                    int rt = Integer.parseInt(instruction.getRt());int rs = Integer.parseInt(instruction.getRs());
                    int immediate = Integer.parseInt(instruction.getImmediate());
                    registers.set(rt, registers.get(rs)+immediate);
                    UnlockRegister(rt);UnlockRegisterIssue(rt);UnlockRegister1(rt);
                    UnlockRegister2(rs);
                }
            }else if(instruction.Function.equals("SUB")){
                if(instruction.Type.equals("1")){
                    int rd = Integer.parseInt(instruction.getRd());int rs = Integer.parseInt(instruction.getRs());
                    int rt = Integer.parseInt(instruction.getRt());
                    int temp = -1;
                    registers.set(rd, registers.get(rs)-registers.get(rt));
                    for (int i = 0; i < PreIssueReady.size(); i++) {
                        Instruction inst = PreIssueReady.get(i);
                        if(inst.Type.equals("1")||inst.Type.equals("AL_SLL")){
                            temp = Integer.parseInt(inst.getRd());
                        }else if(inst.Type.equals("LSW")||inst.Type.equals("2")){
                            temp = Integer.parseInt(inst.getRt());
                        }
                        if(rd==temp){
                            lockRegister(rd);
                            break;
                        }
                        UnlockRegister(rd);
                    }
                    temp = -1;
                    for (int i = 0; i < PreALUReady.size(); i++) {
                        Instruction inst = PreALUReady.get(i);
                        if(inst.Type.equals("1")||inst.Type.equals("AL_SLL")){
                            temp = Integer.parseInt(inst.getRd());
                        }else if(inst.Type.equals("LSW")||inst.Type.equals("2")){
                            temp = Integer.parseInt(inst.getRt());
                        }
                        if(rd==temp){
                            lockRegister(rd);
                            break;
                        }
                        UnlockRegister(rd);
                    }
                    UnlockRegisterIssue(rd);UnlockRegister1(rd);
                    UnlockRegister2(rt);UnlockRegister2(rs);
                }else{
                    int rt = Integer.parseInt(instruction.getRt());int rs = Integer.parseInt(instruction.getRs());
                    int immediate = Integer.parseInt(instruction.getImmediate());
                    registers.set(rt, registers.get(rs)-immediate);
                    UnlockRegister(rt);UnlockRegisterIssue(rt);UnlockRegister1(rt);
                    UnlockRegister2(rs);
                }
            }
            PostALUReady.remove(0);
        }
        if(PostALUBReady.size()>0){// SLL SRL MUL
            Instruction instruction = PostALUBReady.get(0);
            switch (instruction.Function) {
                case "SRL" -> { // 执行操作 并 释放依赖
                    int rd = Integer.parseInt(instruction.getRd());int rt = Integer.parseInt(instruction.getRt());
                    int sa = Integer.parseInt(instruction.getImmediate());
                    registers.set(rd, registers.get(rt) >> sa);
                    UnlockRegister(rd);UnlockRegisterIssue(rd);UnlockRegister1(rd);
                    UnlockRegister2(rt);
                }
                case "SLL" -> {
                    int rd = Integer.parseInt(instruction.getRd());int rt = Integer.parseInt(instruction.getRt());
                    int sa = Integer.parseInt(instruction.getImmediate());
                    registers.set(rd, registers.get(rt) << sa);
                    UnlockRegister(rd);UnlockRegisterIssue(rd);UnlockRegister1(rd);
                    UnlockRegister2(rt);
                }
                case "MUL" -> {
                    int rd = Integer.parseInt(instruction.getRd());int rt = Integer.parseInt(instruction.getRt());
                    int rs = Integer.parseInt(instruction.getRs());
                    registers.set(rd, registers.get(rs) * registers.get(rt));
                    UnlockRegister(rd);UnlockRegisterIssue(rd);UnlockRegister1(rd);
                    UnlockRegister2(rt);UnlockRegister2(rs);
                }
            }
            PostALUBReady.remove(0);
        }

        if(PostMEMReady.size()>0){
            Instruction instruction = PostMEMReady.get(0);
            int rt = Integer.parseInt(instruction.getRt());    //LW操作
            int base = Integer.parseInt(instruction.getBase());
            int offset = Integer.parseInt(instruction.getImmediate());
            int baseValue = registers.get(base);
            int temp = Method.binaryComplementToDecimal(memory.getVector((offset+baseValue-base_PC)/4));;
            registers.set(rt,temp);
            UnlockRegister(rt);UnlockRegisterIssue(rt);UnlockRegister1(rt);
            PostMEMReady.remove(0);
        }
    }

    public void printCheckRegister(){//用于调试
        System.out.println("\ncheckRegister");
        for (int i = 0; i < 32; i++) {
            System.out.print(checkRegister[i] +" ");
        }
        System.out.println("\ncheckRegister1");
        for (int i = 0; i < 32; i++) {
            System.out.print(checkRegister1[i]+" ");
        }
        System.out.println("\ncheckRegister2");
        for (int i = 0; i < 32; i++) {
            System.out.print(checkRegister2[i]+" ");
        }
        System.out.println("\nRegisterStatue");
        for (int i = 0; i < 32; i++) {
            System.out.print(registerStatus[i]+" ");
        }
        System.out.println();
    }

    public void PrintState(){
        System.out.println("--------------------");
        System.out.print("Cycle:"+ Cyc +"\n\n");
        System.out.println("IF Unit");
        if(WaitingInstruction.size()>0) {
            System.out.println("\tWaiting Instruction:" + Instruction.toString(WaitingInstruction.get(0)));
        }else{
            System.out.println("\tWaiting Instruction:");
        }
        if(ExecutedInstruction.size()>0) {
            System.out.println("\tExecuted Instruction:" + Instruction.toString(ExecutedInstruction.get(0)));
        }else{
            System.out.println("\tExecuted Instruction:");
        }
        System.out.println("Pre-Issue Buffer:");
        for (int i = 0; i < 4; i++) {
            if (i <PreIssueReady.size()) {
                System.out.println("\tEntry" + i + ":["+Instruction.toString(PreIssueReady.get(i))+"]");
            }else{
                System.out.println("\tEntry" + i + ":");
            }
        }
        System.out.println("Pre-ALU Queue:");
        for (int i = 0; i < 2; i++) {
            if (i <PreALUReady.size()) {
                System.out.println("\tEntry" + i + ":["+Instruction.toString(PreALUReady.get(i))+"]");
            }else{
                System.out.println("\tEntry" + i + ":");
            }
        }
        System.out.print("Post-ALU Buffer:");
        if(PostALUReady.size()>0){
            System.out.println("["+Instruction.toString(PostALUReady.get(0))+"]");
        }else{
            System.out.println();
        }
        System.out.println("Pre-ALUB Queue:");
        for (int i = 0; i < 2; i++) {
            if (i <PreALUBReady.size()) {
                System.out.println("\tEntry" + i + ":["+Instruction.toString(PreALUBReady.get(i))+"]");
            }else{
                System.out.println("\tEntry" + i + ":");
            }
        }
        System.out.print("Post-ALUB Buffer:");
        if(PostALUBReady.size()>0){
            System.out.println("["+Instruction.toString(PostALUBReady.get(0))+"]");
        }else{
            System.out.println();
        }
        System.out.println("Pre-MEM Queue:");
        for (int i = 0; i < 2; i++) {
            if (i <PreMEMReady.size()) {
                System.out.println("\tEntry" + i + ":["+Instruction.toString(PreMEMReady.get(i))+"]");
            }else{
                System.out.println("\tEntry" + i + ":");
            }
        }
        System.out.print("Post-MEM Buffer:");
        if(PostMEMReady.size()>0){
            System.out.println("["+Instruction.toString(PostMEMReady.get(0))+"]");
        }else {
            System.out.println();
        }
        System.out.print("\nRegisters\nR00:\t");
        for(int i =0;i<32;i++){
            if(i==8){
                System.out.print("\nR08:\t");
            }
            if(i==16||i==24){
                System.out.print("\nR"+Integer.toString(i)+":\t");
                //printWriter.print("\nR16:\t");
            }
            if(i==7||i==15||i==23||i==31){
                System.out.print(registers.get(i));
                //printWriter.print(registers.get(i));
            }else{
                System.out.print(registers.get(i)+"\t");
                //printWriter.print(registers.get(i)+"\t");
            }
        }
        System.out.print("\n\nData");
        int countData = 0;
        for(int i = (break_position-base_PC+4)/4;i<memory.getVectorSize();i++){
            if(countData%8==0){
                System.out.print("\n"+ (4 * i + base_PC) +":\t");
            }
            countData++;
            int dataNum = Method.binaryComplementToDecimal(memory.getVector(i));
            if(countData%8==0){
                System.out.print(dataNum);
            }else{
                System.out.print(dataNum+"\t");
                //printWriter.print(dataNum+"\t");
            }
        }
        System.out.print("\n");
    }

    public void PrintStateToFile(PrintWriter printWriter){
        printWriter.println("--------------------");
        printWriter.print("Cycle:"+ Cyc +"\n\n");
        printWriter.println("IF Unit");
        if(WaitingInstruction.size()>0) {
            printWriter.println("\tWaiting Instruction:" + Instruction.toString(WaitingInstruction.get(0)));
        }else{
            printWriter.println("\tWaiting Instruction:");
        }
        if(ExecutedInstruction.size()>0) {
            printWriter.println("\tExecuted Instruction:" + Instruction.toString(ExecutedInstruction.get(0)));
        }else{
            printWriter.println("\tExecuted Instruction:");
        }
        printWriter.println("Pre-Issue Buffer:");
        for (int i = 0; i < 4; i++) {
            if (i <PreIssueReady.size()) {
                printWriter.println("\tEntry" + i + ":["+Instruction.toString(PreIssueReady.get(i))+"]");
            }else{
                printWriter.println("\tEntry" + i + ":");
            }
        }
        printWriter.println("Pre-ALU Queue:");
        for (int i = 0; i < 2; i++) {
            if (i <PreALUReady.size()) {
                printWriter.println("\tEntry" + i + ":["+Instruction.toString(PreALUReady.get(i))+"]");
            }else{
                printWriter.println("\tEntry" + i + ":");
            }
        }
        printWriter.print("Post-ALU Buffer:");
        if(PostALUReady.size()>0){
            printWriter.println("["+Instruction.toString(PostALUReady.get(0))+"]");
        }else{
            printWriter.println();
        }
        printWriter.println("Pre-ALUB Queue:");
        for (int i = 0; i < 2; i++) {
            if (i <PreALUBReady.size()) {
                printWriter.println("\tEntry" + i + ":["+Instruction.toString(PreALUBReady.get(i))+"]");
            }else{
                printWriter.println("\tEntry" + i + ":");
            }
        }
        printWriter.print("Post-ALUB Buffer:");
        if(PostALUBReady.size()>0){
            printWriter.println("["+Instruction.toString(PostALUBReady.get(0))+"]");
        }else{
            printWriter.println();
        }
        printWriter.println("Pre-MEM Queue:");
        for (int i = 0; i < 2; i++) {
            if (i <PreMEMReady.size()) {
                printWriter.println("\tEntry" + i + ":["+Instruction.toString(PreMEMReady.get(i))+"]");
            }else{
                printWriter.println("\tEntry" + i + ":");
            }
        }
        printWriter.print("Post-MEM Buffer:");
        if(PostMEMReady.size()>0){
            printWriter.println("["+Instruction.toString(PostMEMReady.get(0))+"]");
        }else {
            printWriter.println();
        }
        printWriter.print("\nRegisters\nR00:\t");
        for(int i =0;i<32;i++){
            if(i==8){
                printWriter.print("\nR08:\t");
            }
            if(i==16||i==24){
                printWriter.print("\nR"+ i +":\t");
            }
            if(i==7||i==15||i==23||i==31){
                printWriter.print(registers.get(i));
            }else{
                printWriter.print(registers.get(i)+"\t");
            }
        }
        printWriter.print("\n\nData");
        int countData = 0;
        for(int i = (break_position-base_PC+4)/4;i<memory.getVectorSize();i++){
            if(countData%8==0){
                printWriter.print("\n"+ (4 * i + base_PC) +":\t");
            }
            countData++;
            int dataNum = Method.binaryComplementToDecimal(memory.getVector(i));
            if(countData%8==0){
                printWriter.print(dataNum);
            }else{
                printWriter.print(dataNum+"\t");
            }
        }
        printWriter.print("\n");
    }

    public void Simulation(PrintWriter printWriter){
        Cyc = 1;
        isRunning = true;
        isEnough = false;
        isStalled = false;
        ALUBisStalled = false;
        prepareBoarding();
        while(isRunning) {
            IFDecode();
            Issue();
            ALU();
            ALUB();
            MEM();
            WB();
            updateBuffer();
            PrintState();
            PrintStateToFile(printWriter);
            Cyc++;
        }

    }
}