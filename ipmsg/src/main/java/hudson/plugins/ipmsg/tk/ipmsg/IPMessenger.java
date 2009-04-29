package hudson.plugins.ipmsg.tk.ipmsg;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * @author Naoki Takezoe
 */
public abstract class IPMessenger extends Thread {

    protected String  userName;
    protected String  nickName;
    protected String  group;
    protected String  hostName;
    protected DatagramSocket socket;
    protected boolean absenceMode;
    protected String  absenceMsg;
    protected int     in_port;
    protected boolean debug;
    private   boolean loopFlag;

    // �z�X�g�ƃ��[�U���̃}�b�s���O���s���}�b�v
    private HashMap userNames = new HashMap();

    /** ����M�Ɏg�p���镶���R�[�h */
    private static String CHARSET = "MS932";

    /**
     * �f�t�H���g�̃R���X�g���N�^�B
     */
    public IPMessenger(){
    }

    /** �f�o�b�O���[�h��true�̂Ƃ��W���o�͂Ƀf�o�b�O���b�Z�[�W���o�͂��܂��B*/
    protected void debugMessage(String message){
        if(debug){
            System.out.println(message);
        }
    }

    /**
     * �R���X�g���N�^�B
     *
     * @param userName ���[�U��
     * @param nickName �j�b�N�l�[��
     * @param group    �O���[�v��
     * @param debug    �f�o�b�O���[�h
     */
    public IPMessenger(String userName,String nickName,String group,boolean debug) throws IOException {

        this.userName    = userName;
        this.nickName    = nickName;
        this.group       = group;
        this.hostName    = InetAddress.getLocalHost().getHostName();
        this.absenceMode = false;
        this.absenceMsg  = "";
        this.socket      = new DatagramSocket(Constants.PORT);
        this.in_port     = Constants.PORT;
        this.debug       = debug;
    }

    /**
     * ���O�C�����܂��B
     */
    public void login() throws IOException {
        broadcastMsg(makeTelegram(Constants.IPMSG_BR_ENTRY|Constants.IPMSG_BROADCASTOPT,
                     this.nickName+"\0"+this.group));
        this.loopFlag = true;
    }

    /**
     * ���O�A�E�g���܂��B
     */
    public void logout() throws IOException {
        broadcastMsg(makeTelegram(Constants.IPMSG_BR_EXIT|Constants.IPMSG_BROADCASTOPT,
                     this.nickName+"\0"+this.group));
        this.loopFlag = false;
    }

    /**
     * �s�ݎ҃��[�h
     */
    public void absence(String msg,boolean mode) throws IOException {
        int command;
        if(mode){
            if(msg==null || msg.equals("")){
                this.absenceMsg = "ABSENCE";
            } else {
                this.absenceMsg = "[" + absenceMsg + "]";
            }
            this.absenceMode = true;
            command = Constants.IPMSG_BR_ABSENCE|Constants.IPMSG_ABSENCEOPT;
        } else {
            this.absenceMsg  = "";
            this.absenceMode = false;
            command = Constants.IPMSG_BR_ABSENCE;
        }
        broadcastMsg(makeTelegram(command,this.nickName+this.absenceMsg+"\0"+this.group));
    }

    /**
     * ���b�Z�[�W��M���Ƀt�b�N����钊�ۃ��\�b�h�ł��B
     * �A�v���P�[�V�����ŗL�̏������������Ă��������B
     *
     * @param host ���M�҂̃z�X�g��
     * @param user ���[�U��
     * @param msg ���b�Z�[�W
     * @param lock �������ǂ���
     */
    public abstract void receiveMsg(String host,String user,String msg,boolean lock);

    /**
     * ���b�Z�[�W�𑗐M���܂��B
     */
    public void sendMsg(String host,String msg,boolean secret) throws IOException {
        int mode = 0;
        if(secret){ mode = Constants.IPMSG_SECRETEXOPT; }
        send(makeTelegram(Constants.IPMSG_SENDMSG|mode, msg), host, in_port);
    }

    /**
     * �����o�[�ǉ����Ƀt�b�N����郁�\�b�h�ł��B
     * �A�v���P�[�V�����ŗL�̏������������Ă��������B
     *
     * @param host
     * @param nickName
     * @param group
     * @param addr
     * @param absence
     */
    public abstract void addMember(String host,String nickName,String group,String addr,int absence);

    /**
     * �����o�[�폜���Ƀt�b�N����郁�\�b�h�ł��B
     * �A�v���P�[�V�����ŗL�̏������������Ă��������B
     *
     * @param host
     */
    public abstract void removeMember(String host);

    /**
     * �������b�Z�[�W�̊J�����Ƀt�b�N����郁�\�b�h�ł��B
     * �A�v���P�[�V�����ŗL�̏������������Ă��������B
     *
     * @param host
     * @param user
     */
    public abstract void openMsg(String host,String user);

    /**
     * �J���ʒm�𑗐M���܂��B
     *
     * @param host
     */
    public void readMsg(String host) throws IOException {
        send(makeTelegram(Constants.IPMSG_READMSG|Constants.IPMSG_READCHECKOPT,
                          String.valueOf(new Date().getTime()/1000)),host, in_port);
    }

    /**
     * �T�[�o�̎��s�B���O�C�����start���\�b�h������s���Ă��������B
     */
    public void run(){
        try {
            while(loopFlag){
                byte[] buf = new byte[Constants.BUFSIZE];
                DatagramPacket packet = new DatagramPacket(buf,buf.length);
                this.socket.receive(packet);
                new ChildThread(packet).start();
            }
        } catch(IOException ex){
            ex.printStackTrace();
        }

        this.socket.close();
    }

    /**
     * ���b�Z�[�W�𑗐M���܂��B
     *
     * @param msg ���b�Z�[�W
     * @param host ���M��̃z�X�g��
     * @param port ���M��̃|�[�g�ԍ�
     */
    private void send(String msg,String host,int port) throws IOException {
        String message = msg;
        byte[] byteMsg = message.getBytes(CHARSET);
        DatagramPacket packet = new DatagramPacket(byteMsg,byteMsg.length,
                                                   InetAddress.getByName(host),port);
        socket.send(packet);
    }

    /**
     *
     */
    private String makeTelegram(int command,String supplement){

        StringBuffer sb = new StringBuffer();

        sb.append(Constants.PROTOCOL_VER);
        sb.append(":");
        sb.append(new Date().getTime()/1000);
        sb.append(":");
        sb.append(this.userName);
        sb.append(":");
        sb.append(this.hostName);
        sb.append(":");
        sb.append(command);
        sb.append(":");
        sb.append(supplement);

        return sb.toString();
    }

    /**
     *
     */
    private void broadcastMsg(String msg) throws IOException {
        String message = msg;
        byte[] byteMsg = message.getBytes(CHARSET);

        DatagramPacket packet = new DatagramPacket(byteMsg,byteMsg.length,
                                                   InetAddress.getByName("255.255.255.255"),
                                                   in_port);
        this.socket.send(packet);
    }

    /**
     * ��������w�肵��������ŕ������A�z��ŕԂ��܂��B
     *
     * @param s1 �����Ώۂ̕�����B
     * @param s2 �������̋�؂�Ɏg�p���镶����B
     * @return �������ʂ�z��ŕԂ��܂��Bs1��null�̏ꍇ��null��Ԃ��܂��B
     */
    private static String[] split(String s1,String s2){
        if(s1 == null){
            return null;
        }
        ArrayList v  = new ArrayList();
        int last  = 0;
        int index = 0;
        while((index=s1.indexOf(s2,last))!=-1){
            v.add(s1.substring(last,index));
            last = index + s2.length();
        }
        if(last!=s1.length()){
            v.add(s1.substring(last));
        }
        return (String[])v.toArray(new String[v.size()]);
    }

    /**
     * ��M�����p�P�b�g��������������X���b�h�N���X�B
     */
    private class ChildThread extends Thread {

    	private DatagramPacket packet;

    	/** �R���X�g���N�^�B*/
    	public ChildThread(DatagramPacket packet){
    		this.packet = packet;
    	}

    	/** �p�P�b�g���������܂��B*/
    	public void run(){
    		try {
    			String message = new String(packet.getData(),CHARSET);
    			debugMessage("[MSG]" + message.trim());

                String[] telegram = split(message,":");

                int command = 0;
                command = Integer.parseInt(telegram[4]);
                int cmd_no  = command & 0x000000ff;

                InetAddress from = packet.getAddress();
                String fromAddr = split(from.toString(),"/")[1];
                String fromHost = fromAddr;
                int    fromPort = packet.getPort();
                int    packetNo = Integer.parseInt(telegram[1]);

                /* port�ԍ�������Ă����珈�����Ȃ� */
                if (fromPort != in_port){
                    return;
                }

                switch(cmd_no){
                    case Constants.IPMSG_ANSENTRY: {
                        String[] dim = split(telegram[5], "\0");
                        if (dim[0].equals("")) {
                            dim[0] = telegram[2];
                        }
                        userNames.put(fromHost,dim[0]);
                        addMember(fromHost, dim[0], dim[1], fromAddr,
                                  Constants.IPMSG_ABSENCEOPT & command);
                        break;
                    }
                    case Constants.IPMSG_BR_ENTRY: {
                        String[] dim = split(telegram[5], "\0");
                        userNames.put(fromHost,dim[0]);
                        addMember(fromHost, dim[0], dim[1], fromAddr,
                                  Constants.IPMSG_ABSENCEOPT & command);
                        if (absenceMode) {
                            send(makeTelegram(Constants.IPMSG_ANSENTRY | Constants.IPMSG_ABSENCEOPT,
                                              nickName + absenceMsg + "\0" + group), fromAddr, fromPort);
                        }
                        else {
                            send(makeTelegram(Constants.IPMSG_ANSENTRY,nickName + "\0" + group),
                                 fromAddr, fromPort);
                        }
                        break;
                    }
                    case Constants.IPMSG_SENDMSG: {
                        boolean lockFlag = false;
                        if ( (command & Constants.IPMSG_SENDCHECKOPT) != 0) {
                            int ack_cmd = Constants.IPMSG_RECVMSG;
                            send(makeTelegram(ack_cmd,String.valueOf(packetNo)),
                                              fromAddr, in_port);
                        }
                        if ( (command & Constants.IPMSG_SECRETOPT) != 0) {
                            int ack_cmd = Constants.IPMSG_RECVMSG;
                            send(makeTelegram(ack_cmd,String.valueOf(packetNo)),
                                              fromAddr, in_port);
                            lockFlag = true;
                        }
                        // :�ŋ�؂�ꂽ���̂�߂�
                        int in_length = telegram.length;
                        if(telegram.length > 6){
                            for(int j = 6;j < in_length ;j++){
                                telegram[5] += ":" + telegram[j];
                            }
                        }
                        String nickName = (String)userNames.get(fromHost);
                        //receiveMsg(fromHost, telegram[2], telegram[5].trim(),lockFlag);
                        receiveMsg(fromHost, nickName, telegram[5].trim(),lockFlag);
                        break;
                    }

                    case Constants.IPMSG_READMSG: {
                        if ( (command & Constants.IPMSG_READCHECKOPT) != 0) {
                            int ack_cmd = Constants.IPMSG_ANSREADMSG;
                            send(makeTelegram(ack_cmd,String.valueOf(packetNo)),
                                              fromAddr, in_port);
                            openMsg(fromHost, telegram[2]);
                        }
                        break;
                    }
                    case Constants.IPMSG_BR_ABSENCE: {
                        String[] dim = split(telegram[5], "\0");
                        addMember(fromHost, dim[0], dim[1], fromAddr,
                                  Constants.IPMSG_ABSENCEOPT & command);
                        break;
                    }

                    case Constants.IPMSG_BR_EXIT: {
                        userNames.remove(fromHost);
                        removeMember(fromHost);
                        break;
                    }
                }
            } catch(IOException ex){
               ex.printStackTrace();
            } finally {
            }
    	}
    }
}
