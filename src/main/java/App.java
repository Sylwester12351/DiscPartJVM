import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

public class App {
    private JPanel panel1;
    private JTextArea textAreaInformation;
    private JList<String> discList;
    private DefaultListModel<String> listModel = new DefaultListModel<String>();
    private JButton testButton; // this button is hidden only for test
    private JButton refreshButton;
    private JButton format;
    private JRadioButton radioButtonFAT32;
    private JRadioButton radioButtonNTFS;
    private JProgressBar progressBar1;
    private JLabel systemProp;
    private JLabel versionApp;
    private JLabel mouResult;
    private JButton mountUnmount;
    private JLabel FileSysLab;
    private JTextField textCommand; // this field is hidden only for test
    private Runtime runtime = Runtime.getRuntime();
    ExecuteCommand executeCommand = new ExecuteCommand();
    VerApp verApp = new VerApp();

    private String selected,pcent;

    private String mountResult;
    private String mountResultFix;
    private String fileSys;
    private String fileSysFix;
    private int ntfsMounted = 0;
    private int otherMounted = 0;

    public String getSelected() {
        return selected;
    }

    public App() {
        systemProp.setText(System.getProperty("os.name") + " " + System.getProperty("os.version") + " " + System.getProperty("os.arch"));
        versionApp.setText(verApp.getVer());

        discList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!discList.isSelectionEmpty()){
                    selected = discList.getSelectedValue().toString();
                }
                executeCommand.commands("lsblk -io SIZE,TYPE,MODEL " + selected);
                StringBuilder result = executeCommand.getResult();
                result.replace(0,17," -- ");
                textAreaInformation.append(result.toString()+"\n");

                //Filesys
                executeCommand.commands("lsblk -io FSTYPE " + selected);
                fileSys = executeCommand.getResult().toString();
                fileSysFix = fileSys.replace("FSTYPE","");
                FileSysLab.setText(fileSysFix.toString());

                //Mount point
                executeCommand.commands("lsblk -io MOUNTPOINT " + selected);
                mountResult = executeCommand.getResult().toString();
                mountResultFix = mountResult.replace("MOUNTPOINT","");
                if (mountResultFix.toString().isEmpty()){
                    mountUnmount.setText("Zamontuj");
                }else {
                    mountUnmount.setText("Odmontuj");
                }
                mouResult.setText(mountResultFix.toString());
                // show usage
                executeCommand.commands("df -h " + selected + " --output=pcent");
                StringBuilder a = executeCommand.getResult();
                if (!StringUtils.isBlank(a)) { // TODO przenieść do nowej klasy
                    a.replace(0,4,"usag");
                    a.append("end");
                    int startIndex = a.indexOf("usag") + 4;
                    int endIndex = a.indexOf("%end");
                    pcent = a.substring(startIndex,endIndex);
                }
                int value = Integer.parseInt(pcent.trim());
                if (value < 70){
                    progressBar1.setValue(value);
                    progressBar1.setForeground(Color.BLUE);

                }else {
                    progressBar1.setValue(value);
                    progressBar1.setForeground(Color.RED);
                }
            }
        });

        format.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selected.isEmpty() || !radioButtonFAT32.isSelected() && !radioButtonNTFS.isSelected()){
                    textAreaInformation.append("Nic nie zostało wybrane"+"\n");
                }
                if (radioButtonNTFS.isSelected() && radioButtonFAT32.isSelected()){
                    textAreaInformation.append("Nie mogę tego wykonać zaznaczono FAT32 i NTFS !"+"\n");
                }
                // NTFS
                if (radioButtonNTFS.isSelected() && !radioButtonFAT32.isSelected()){
                    textAreaInformation.append("Program chwilowo nie działa na wielu wątkach więc okno programu może się zawiesić w czasie formatowania "+"\n");
                    executeCommand.commands("mkfs.ntfs -f "+ selected);
                    textAreaInformation.append(executeCommand.getResult()+"\n");
                }
                // FAT32
                if (!radioButtonNTFS.isSelected() && radioButtonFAT32.isSelected()) {
                    textAreaInformation.append("Program chwilowo nie działa na wielu wątkach więc okno programu może się zawiesić w czasie formatowania "+"\n");
                    executeCommand.commands("mkfs.vfat " + selected);
                    textAreaInformation.append(executeCommand.getResult() + "\n");
                }
            }
        });
        mountUnmount.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (fileSysFix.equals("ntfs")){
                    if (mountResultFix.toString().isEmpty()){
                        ntfsMounted = ntfsMounted + 1;

                        executeCommand.commands("mkdir /mnt/ntfs"+ntfsMounted);
                        executeCommand.commands("mount -t ntfs "+selected+ " /mnt/ntfs"+ntfsMounted);
                        textAreaInformation.append(executeCommand.getResult().toString());
                        refreshDisc();
                    }else {
                        executeCommand.commands("umount "+selected);
                        textAreaInformation.append(executeCommand.getResult().toString());
                        refreshDisc();
                    }
                }else {
                    if (mountResultFix.toString().isEmpty()){
                        otherMounted = otherMounted + 1;

                        executeCommand.commands("mkdir /mnt/other"+otherMounted);
                        executeCommand.commands("mount "+selected+" /mnt/other"+otherMounted);
                        textAreaInformation.append(executeCommand.getResult().toString());
                        refreshDisc();
                    }else {
                        executeCommand.commands("umount "+selected);
                        textAreaInformation.append(executeCommand.getResult().toString());
                        refreshDisc();
                    }
                }
            }
        });
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshDisc();
            }
        });
    }
    public static void main(String[] args) {
        ImageIcon imageIcon = new ImageIcon("ProgramIcon.png");
        JFrame frame = new JFrame("DiscPartJVM");
        frame.setContentPane(new App().panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setIconImage(imageIcon.getImage());
        frame.pack();
        frame.setSize(750, 550);
        frame.setResizable(false);
        frame.setVisible(true);


    }

    public void refreshDisc(){
        discList.updateUI();
        listModel.clear();
        try {
            Process process = runtime.exec("lsblk -np --output KNAME");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            try {
                process.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            String line;
            while ((line = reader.readLine()) !=null){
                listModel.addElement(line);
            }
            discList.setModel(listModel);
            textAreaInformation.setText("Wykonano polecenie : lsblk -np --output KNAME"+"\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}