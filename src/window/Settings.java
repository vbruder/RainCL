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

public class Settings extends JDialog implements TimerListener
{
    private static final long serialVersionUID = 1L;

    private static Settings mDial;

    private final JPanel settingsPanel = new JPanel();
    private JTextField txtFPS;

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
        setBounds(100, 100, 500, 500);
        getContentPane().setLayout(new BorderLayout());
        settingsPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        getContentPane().add(settingsPanel, BorderLayout.CENTER);
        GridBagLayout gbl_contentPanel = new GridBagLayout();
        gbl_contentPanel.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        gbl_contentPanel.rowHeights = new int[]{0, 0, 0};
        gbl_contentPanel.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
        gbl_contentPanel.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
        settingsPanel.setLayout(gbl_contentPanel);
        {
            JLabel lblFps = new JLabel("FPS:");
            GridBagConstraints gbc_lblFps = new GridBagConstraints();
            gbc_lblFps.insets = new Insets(0, 0, 0, 5);
            gbc_lblFps.anchor = GridBagConstraints.EAST;
            gbc_lblFps.gridx = 13;
            gbc_lblFps.gridy = 1;
            settingsPanel.add(lblFps, gbc_lblFps);
        }
        {
            txtFPS = new JTextField();
            txtFPS.setEditable(false);
            GridBagConstraints gbc_txtFPS = new GridBagConstraints();
            gbc_txtFPS.fill = GridBagConstraints.HORIZONTAL;
            gbc_txtFPS.gridx = 14;
            gbc_txtFPS.gridy = 1;
            settingsPanel.add(txtFPS, gbc_txtFPS);
            txtFPS.setColumns(10);
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
        txtFPS.setText(Float.toString( Rain.getFPS() ));
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
