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
import java.awt.GridLayout;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.factories.FormFactory;

public class Settings extends JDialog implements TimerListener
{
    private static final long serialVersionUID = 1L;

    private static Settings mDial;

    private final JPanel settingsPanel = new JPanel();
    private JTextField txtFPS;
    private JTextField txtParticles;
    private JTextField txtRainclA;
    private JTextField txtGraphics;
    private JTextField txtDriver;
    private JTextField txtOpenGL;
    private JTextField txtShadingLang;
    private JTextField txtOpenCL;

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
        setBounds(100, 100, 425, 450);
        getContentPane().setLayout(new BorderLayout());
        settingsPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        getContentPane().add(settingsPanel, BorderLayout.CENTER);
        GridBagLayout gbl_contentPanel = new GridBagLayout();
        gbl_contentPanel.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 36, 0, 0};
        gbl_contentPanel.rowHeights = new int[]{0, 0, 0, 0};
        gbl_contentPanel.columnWeights = new double[]{0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
        gbl_contentPanel.rowWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
        settingsPanel.setLayout(gbl_contentPanel);
        {
            JLabel lblParticles = new JLabel("Particles:");
            GridBagConstraints gbc_lblParticles = new GridBagConstraints();
            gbc_lblParticles.anchor = GridBagConstraints.EAST;
            gbc_lblParticles.insets = new Insets(0, 0, 5, 5);
            gbc_lblParticles.gridx = 9;
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
            gbc_txtParticles.gridx = 10;
            gbc_txtParticles.gridy = 0;
            settingsPanel.add(txtParticles, gbc_txtParticles);
        }
        {
            JLabel lblFPS = new JLabel("FPS:");
            GridBagConstraints gbc_lblFPS = new GridBagConstraints();
            gbc_lblFPS.anchor = GridBagConstraints.EAST;
            gbc_lblFPS.insets = new Insets(0, 0, 5, 5);
            gbc_lblFPS.gridx = 11;
            gbc_lblFPS.gridy = 0;
            settingsPanel.add(lblFPS, gbc_lblFPS);
        }
        {
            txtFPS = new JTextField();
            txtFPS.setEditable(false);
            txtFPS.setColumns(10);
            GridBagConstraints gbc_txtFPS = new GridBagConstraints();
            gbc_txtFPS.anchor = GridBagConstraints.EAST;
            gbc_txtFPS.insets = new Insets(0, 0, 5, 0);
            gbc_txtFPS.gridx = 12;
            gbc_txtFPS.gridy = 0;
            settingsPanel.add(txtFPS, gbc_txtFPS);
        }
        {
            JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
            GridBagConstraints gbc_tabbedPane = new GridBagConstraints();
            gbc_tabbedPane.gridheight = 2;
            gbc_tabbedPane.gridwidth = 13;
            gbc_tabbedPane.fill = GridBagConstraints.BOTH;
            gbc_tabbedPane.gridx = 0;
            gbc_tabbedPane.gridy = 1;
            settingsPanel.add(tabbedPane, gbc_tabbedPane);
            {
                JLayeredPane lpGeneral = new JLayeredPane();
                tabbedPane.addTab("General", null, lpGeneral, null);
            }
            
            JLayeredPane lpLighting = new JLayeredPane();
            tabbedPane.addTab("Lighting", null, lpLighting, null);
            
            JLayeredPane lpSysInfo = new JLayeredPane();
            tabbedPane.addTab("System Info", null, lpSysInfo, null);
            lpSysInfo.setLayout(new FormLayout(new ColumnSpec[] {
                    FormFactory.RELATED_GAP_COLSPEC,
                    FormFactory.DEFAULT_COLSPEC,
                    FormFactory.RELATED_GAP_COLSPEC,
                    FormFactory.DEFAULT_COLSPEC,
                    FormFactory.RELATED_GAP_COLSPEC,
                    ColumnSpec.decode("default:grow"),},
                new RowSpec[] {
                    FormFactory.RELATED_GAP_ROWSPEC,
                    FormFactory.DEFAULT_ROWSPEC,
                    FormFactory.RELATED_GAP_ROWSPEC,
                    FormFactory.DEFAULT_ROWSPEC,
                    FormFactory.RELATED_GAP_ROWSPEC,
                    FormFactory.DEFAULT_ROWSPEC,
                    FormFactory.RELATED_GAP_ROWSPEC,
                    FormFactory.DEFAULT_ROWSPEC,
                    FormFactory.RELATED_GAP_ROWSPEC,
                    FormFactory.DEFAULT_ROWSPEC,
                    FormFactory.RELATED_GAP_ROWSPEC,
                    FormFactory.DEFAULT_ROWSPEC,}));
            {
                JLabel lblGraphics = new JLabel("Graphics:");
                lpSysInfo.add(lblGraphics, "4, 4, left, default");
            }
            {
                txtGraphics = new JTextField();
                txtGraphics.setEditable(false);
                lpSysInfo.add(txtGraphics, "6, 4, fill, default");
                txtGraphics.setColumns(10);
            }
            {
                JLabel lblDriver = new JLabel("Driver:");
                lpSysInfo.add(lblDriver, "4, 6, left, default");
            }
            {
                txtDriver = new JTextField();
                txtDriver.setEditable(false);
                lpSysInfo.add(txtDriver, "6, 6, fill, default");
                txtDriver.setColumns(10);
            }
            {
                JLabel lblOpengl = new JLabel("OpenGL:");
                lpSysInfo.add(lblOpengl, "4, 8, left, default");
            }
            {
                txtOpenGL = new JTextField();
                txtOpenGL.setEditable(false);
                lpSysInfo.add(txtOpenGL, "6, 8, fill, default");
                txtOpenGL.setColumns(10);
            }
            {
                JLabel lblShadingLanguage = new JLabel("Shading Language:");
                lpSysInfo.add(lblShadingLanguage, "4, 10, left, default");
            }
            {
                txtShadingLang = new JTextField();
                txtShadingLang.setEditable(false);
                lpSysInfo.add(txtShadingLang, "6, 10, fill, default");
                txtShadingLang.setColumns(10);
            }
            {
                JLabel lblOpencl = new JLabel("OpenCL:");
                lpSysInfo.add(lblOpencl, "4, 12, left, default");
            }
            {
                txtOpenCL = new JTextField();
                txtOpenCL.setEditable(false);
                lpSysInfo.add(txtOpenCL, "6, 12, fill, default");
                txtOpenCL.setColumns(10);
            }
            
            JLayeredPane lpAbout = new JLayeredPane();
            tabbedPane.addTab("About", null, lpAbout, null);
            
            txtRainclA = new JTextField();
            txtRainclA.setEditable(false);
            txtRainclA.setText("RainCL - A rain simulation framework.\r\n\r\nVersion: 0.1\r\n\r\n(c) Valentin Bruder, Universit\u00E4t Osnabr\u00FCck, 2013\r\n\r\nThis framework uses LWJGL (www.lwjgl.org) and slick-util libraries.");
            txtRainclA.setBounds(10, 11, 384, 303);
            lpAbout.add(txtRainclA);
            txtRainclA.setColumns(10);
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
