import javax.swing.*;

public class Interface {
    private JButton start;
    private JPanel mainPanel;
    private JComboBox comboBox1;

    public Interface() {
        start.addActionListener(e -> {
            try {
                Dumper dumper = new Dumper(Server.GOLEMAG,0.032);
                dumper.start();
                while (true) {
                    dumper.doStuff();
                    Thread.sleep(8000);
                }
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        });

    }
    public static void main(String[] args) {
        JFrame frame = new JFrame("Dumper");
        frame.setContentPane(new Interface().mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
