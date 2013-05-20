package window;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import java.awt.GridBagLayout;
import javax.swing.JLabel;
import java.awt.GridBagConstraints;
import javax.swing.JTextField;

import main.Rain;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.JTabbedPane;
import javax.swing.JLayeredPane;

public class Settings extends JDialog implements TimerListener
{
    private static final long serialVersionUID = 1L;

    private static Settings mDial;

    private final JPanel settingsPanel = new JPanel();
    private JTextField txtFPS;
    private JTextField txtParticles;

    /**
     * Launch the application.
     */
    public void run()
    {
        try
        {
            this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            this.setVisible(true);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Create the dialog.
     */
    public Settings()
    {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) { }
        
        setResizable(false);
        
        Action closeAction = new AbstractAction(){
            private static final long serialVersionUID = 2L;
            public void actionPerformed(ActionEvent e) {
                destroyInstance();
            }
        };
        
        settingsPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "closeAction");
        settingsPanel.getActionMap().put("closeAction", closeAction);
        
        setTitle("Settings");
        setBounds(100, 100, 400, 450);
        getContentPane().setLayout(new BorderLayout());
        settingsPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        getContentPane().add(settingsPanel, BorderLayout.CENTER);
        GridBagLayout gbl_contentPanel = new GridBagLayout();
        gbl_contentPanel.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 36, 0, 0, 0, 0};
        gbl_contentPanel.rowHeights = new int[]{0, 0, 0, 0};
        gbl_contentPanel.columnWeights = new double[]{0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
        gbl_contentPanel.rowWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
        settingsPanel.setLayout(gbl_contentPanel);
        {
            JLabel lblParticles = new JLabel("Particles:");
            GridBagConstraints gbc_lblParticles = new GridBagConstraints();
            gbc_lblParticles.anchor = GridBagConstraints.EAST;
            gbc_lblParticles.insets = new Insets(0, 0, 5, 5);
            gbc_lblParticles.gridx = 10;
            gbc_lblParticles.gridy = 0;
            settingsPanel.add(lblParticles, gbc_lblParticles);
        }
        {
            txtParticles = new JTextField();
            txtParticles.setEditable(false);
            txtParticles.setColumns(10);
            GridBagConstraints gbc_txtParticles = new GridBagConstraints();
            gbc_txtParticles.anchor = GridBagConstraints.EAST;
            gbc_txtParticles.insets = new Insets(0, 0, 5, 5);
            gbc_txtParticles.gridx = 11;
            gbc_txtParticles.gridy = 0;
            settingsPanel.add(txtParticles, gbc_txtParticles);
        }
        {
            JLabel lblFPS = new JLabel("FPS:");
            GridBagConstraints gbc_lblFPS = new GridBagConstraints();
            gbc_lblFPS.anchor = GridBagConstraints.EAST;
            gbc_lblFPS.insets = new Insets(0, 0, 5, 5);
            gbc_lblFPS.gridx = 12;
            gbc_lblFPS.gridy = 0;
            settingsPanel.add(lblFPS, gbc_lblFPS);
        }
        {
            txtFPS = new JTextField();
            txtFPS.setEditable(false);
            txtFPS.setColumns(10);
            GridBagConstraints gbc_txtFPS = new GridBagConstraints();
            gbc_txtFPS.anchor = GridBagConstraints.EAST;
            gbc_txtFPS.insets = new Insets(0, 0, 5, 5);
            gbc_txtFPS.gridx = 13;
            gbc_txtFPS.gridy = 0;
            settingsPanel.add(txtFPS, gbc_txtFPS);
        }
        {
            JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
            GridBagConstraints gbc_tabbedPane = new GridBagConstraints();
            gbc_tabbedPane.gridheight = 2;
            gbc_tabbedPane.gridwidth = 14;
            gbc_tabbedPane.fill = GridBagConstraints.BOTH;
            gbc_tabbedPane.gridx = 1;
            gbc_tabbedPane.gridy = 1;
            settingsPanel.add(tabbedPane, gbc_tabbedPane);
            {
                JLayeredPane layeredPane = new JLayeredPane();
                tabbedPane.addTab("General", null, layeredPane, null);
            }
            
            JLayeredPane layeredPane = new JLayeredPane();
            tabbedPane.addTab("Lighting", null, layeredPane, null);
            
            JLayeredPane layeredPane_1 = new JLayeredPane();
            tabbedPane.addTab("System Info", null, layeredPane_1, null);
            
            JLayeredPane layeredPane_2 = new JLayeredPane();
            tabbedPane.addTab("About", null, layeredPane_2, null);
        }
        {
            JPanel buttonPane = new JPanel();
            buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
            getContentPane().add(buttonPane, BorderLayout.SOUTH);
            {
                JButton okButton = new JButton("OK");
                okButton.setActionCommand("OK");
                buttonPane.add(okButton);
                getRootPane().setDefaultButton(okButton);
            }
            {
                JButton cancelButton = new JButton("Cancel");
                cancelButton.setActionCommand("Cancel");
                buttonPane.add(cancelButton);
            }
        }
        
        
        
        this.run();
    }
    
    @Override
    public void updateTex()
    {
        txtFPS.setText(Integer.toString( (int)Rain.getFPS() ));
    }
    
    public void close()
    {
        this.dispose();
        mDial = null;
    }
    
    /**
     * @brief returns instance of the object if not already existing (singleton pattern) 
     */
    public static Settings getInstance()
    {
        if(mDial != null)
        {
            mDial.toFront();
            return mDial;
        }
        else
        {
            mDial = new Settings();
            return mDial;
        }
        
    }
    public static void destroyInstance()
    {
        if(mDial != null)
        {
            mDial.close();
        }
    }
}
