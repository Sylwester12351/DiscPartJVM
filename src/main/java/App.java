import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
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
    private JButton showDiscInfo;
    private JButton format;
    private JRadioButton radioButtonFAT32;
    private JRadioButton radioButtonNTFS;
    private JTextField textCommand; // this field is hidden only for test
    private Runtime runtime = Runtime.getRuntime();
    private Process process;
    private BufferedReader reader;
    ExecuteCommand executeCommand = new ExecuteCommand();

    private String selected;

    public App() {
        refreshButton.addActionListener(e -> refreshDisc());
        discList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                selected = discList.getSelectedValue().toString();
            }
        });
        showDiscInfo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selected !=null) {
                    executeCommand.commands("df -h " + selected + " --output=source,fstype,size,used,avail,pcent");
                    textAreaInformation.append(executeCommand.getResult() + "\n");
                }else {
                    textAreaInformation.append("Nic nie wybrałeś !"+"\n");
                }
            }
        });
        format.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selected.isEmpty()){
                    textAreaInformation.append("Nic nie zostało wybrane"+"\n");
                }
                if (radioButtonNTFS.isSelected() && radioButtonFAT32.isSelected()){
                    textAreaInformation.append("Nie mogę tego wykonać zaznaczono FAT32 i NTFS !"+"\n");
                }
                // NTFS
                if (radioButtonNTFS.isSelected() && !radioButtonFAT32.isSelected()){
                    executeCommand.commands("mkfs.ntfs -f "+ selected);
                    textAreaInformation.append(executeCommand.getResult()+"\n");
                }
                // FAT32
                if (!radioButtonNTFS.isSelected() && radioButtonFAT32.isSelected()) {
                    executeCommand.commands("mkfs.vfat " + selected);
                    textAreaInformation.append(executeCommand.getResult() + "\n");
                }
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
        listModel.clear();
        try {
            process = runtime.exec("lsblk -np --output KNAME");
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
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
            textAreaInformation.setText("Wykonano polecenie : lsblk -np --output KNAME "+"\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
