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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.awt.GridBagLayout;
import javax.swing.JLabel;
import java.awt.GridBagConstraints;
import javax.swing.JTextField;

import main.Rain;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import javax.swing.JTabbedPane;
import javax.swing.JLayeredPane;
import java.awt.GridLayout;

import apiWrapper.GL;
import apiWrapper.OpenCL;

import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.factories.FormFactory;

import environment.Rainstreaks;

import java.awt.Font;
import javax.swing.JSlider;
import java.awt.Color;
import java.text.NumberFormat;

import javax.swing.JCheckBox;

public class Settings extends JDialog implements TimerListener
{
    private static final long serialVersionUID = 1L;

    private static Settings mDial;

    private final JPanel settingsPanel = new JPanel();
    private JTextField txtFPS;
    private JTextField txtParticles;
    private JTextField txtGraphics;
    private JTextField txtDriver;
    private JTextField txtOpenGL;
    private JTextField txtShadingLang;
    private JTextField txtOpenCL;
    
    private String sysGraphics;
    private String sysDriver;
    private String sysOpenGL;
    private String sysShadingLang;
    private String sysOpenCL;

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
        
        sysGraphics     = GL.getRenderer();
        sysDriver       = GL.getDriverversion();
        sysOpenGL       = GL.getVersion();
        sysShadingLang  = GL.getShadinglang();
        sysOpenCL       = OpenCL.getVersion();
        
        settingsPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "closeAction");
        settingsPanel.getActionMap().put("closeAction", closeAction);
        
        setTitle("Settings");
        setBounds(100, 100, 427, 399);
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
                
                JLabel lblParticles = new JLabel("Rain:");
                lblParticles.setBounds(10, 28, 46, 26);
                lpGeneral.add(lblParticles);
                
                final JSlider slrParticles = new JSlider();
                slrParticles.setValue(15);
                slrParticles.setMinimum(10);
                slrParticles.setMaximum(20);
                slrParticles.setMinorTickSpacing(1);
                slrParticles.setBackground(Color.WHITE);
                slrParticles.setSnapToTicks(true);
                slrParticles.setPaintTicks(true);
                slrParticles.setBounds(66, 28, 328, 26);
                lpGeneral.add(slrParticles);
                slrParticles.addChangeListener(new ChangeListener(){
                    public void stateChanged(ChangeEvent e) {
                        int newValue = (1 << slrParticles.getValue());
                        environment.Rainstreaks.setMaxParticles(newValue);                 
                    }
                });
                
                JLabel lblWind = new JLabel("Wind:");
                lblWind.setBounds(10, 83, 46, 26);
                lpGeneral.add(lblWind);
                
                final JSlider slrWind = new JSlider();
                slrWind.setMinorTickSpacing(10);
                slrWind.setPaintTicks(true);
                slrWind.setBackground(Color.WHITE);
                slrWind.setBounds(66, 83, 328, 26);
                lpGeneral.add(slrWind);
                slrWind.addChangeListener(new ChangeListener(){
                    public void stateChanged(ChangeEvent e) {
                        float newValue = ((float) slrWind.getValue()) / 16.0f;
                        environment.Rainstreaks.setWindForce(newValue);                 
                    }
                });
                
                JCheckBox cbxSound = new JCheckBox("Sound");
                cbxSound.setBackground(Color.WHITE);
                cbxSound.setBounds(10, 174, 91, 23);
                lpGeneral.add(cbxSound);
                cbxSound.addItemListener(new ItemListener() {
                    public void itemStateChanged(ItemEvent e) {
                        boolean temp;
                        if (e.getStateChange() == 1)
                            temp = true;
                        else
                            temp = false;
                        main.Rain.setAudio(temp);
                    }
                });
                cbxSound.setSelected(main.Rain.isAudio());
                
                JCheckBox cbxSPH = new JCheckBox("SPH");
                cbxSPH.setBackground(Color.WHITE);
                cbxSPH.setBounds(10, 200, 160, 23);
                lpGeneral.add(cbxSPH);
                {
                    JCheckBox cbxFog = new JCheckBox("Fog");
                    cbxFog.setBackground(Color.WHITE);
                    cbxFog.setBounds(10, 148, 91, 23);
                    lpGeneral.add(cbxFog);
                }
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
                JLabel lblGraphics = new JLabel("Graphics Hardware:");
                lpSysInfo.add(lblGraphics, "2, 4, left, default");
            }
            {
                txtGraphics = new JTextField();
                txtGraphics.setBackground(Color.WHITE);
                txtGraphics.setEditable(false);
                lpSysInfo.add(txtGraphics, "4, 4, fill, default");
                txtGraphics.setColumns(30);
                txtGraphics.setText(sysGraphics);
            }
            {
                JLabel lblDriver = new JLabel("Driver Version:");
                lpSysInfo.add(lblDriver, "2, 6, left, default");
            }
            {
                txtDriver = new JTextField();
                txtDriver.setBackground(Color.WHITE);
                txtDriver.setEditable(false);
                lpSysInfo.add(txtDriver, "4, 6, fill, default");
                txtDriver.setColumns(30);
                txtDriver.setText(sysDriver);
            }
            {
                JLabel lblOpengl = new JLabel("OpenGL Version:");
                lpSysInfo.add(lblOpengl, "2, 8, left, default");
            }
            {
                txtOpenGL = new JTextField();
                txtOpenGL.setBackground(Color.WHITE);
                txtOpenGL.setEditable(false);
                lpSysInfo.add(txtOpenGL, "4, 8, fill, default");
                txtOpenGL.setColumns(30);
                txtOpenGL.setText(sysOpenGL);
            }
            {
                JLabel lblShadingLanguage = new JLabel("Shading Language:");
                lpSysInfo.add(lblShadingLanguage, "2, 10, left, default");
            }
            {
                txtShadingLang = new JTextField();
                txtShadingLang.setBackground(Color.WHITE);
                txtShadingLang.setEditable(false);
                lpSysInfo.add(txtShadingLang, "4, 10, fill, default");
                txtShadingLang.setColumns(30);
                txtShadingLang.setText(sysShadingLang);
            }
            {
                JLabel lblOpencl = new JLabel("OpenCL Version:");
                lpSysInfo.add(lblOpencl, "2, 12, left, default");
            }
            {
                txtOpenCL = new JTextField();
                txtOpenCL.setBackground(Color.WHITE);
                txtOpenCL.setEditable(false);
                lpSysInfo.add(txtOpenCL, "4, 12");
                txtOpenCL.setColumns(30);
                txtOpenCL.setText(sysOpenCL);
            }
            
            JLayeredPane lpAbout = new JLayeredPane();
            tabbedPane.addTab("About", null, lpAbout, null);
            
            JLabel lblNewLabel = new JLabel("RainCL - A rain simulation framework.");
            lblNewLabel.setFont(new Font("Tahoma", Font.BOLD, 11));
            lblNewLabel.setBounds(10, 11, 384, 20);
            lpAbout.add(lblNewLabel);
            
            JLabel lblVersionprealpha = new JLabel("Version: 0.1 (Pre-Alpha)");
            lblVersionprealpha.setBounds(10, 42, 384, 14);
            lpAbout.add(lblVersionprealpha);
            
            JLabel lblNewLabel_1 = new JLabel("(c) Valentin Bruder, Universit\u00E4t Osnabr\u00FCck, 2013");
            lblNewLabel_1.setBounds(10, 67, 384, 14);
            lpAbout.add(lblNewLabel_1);
            
            JLabel lblNewLabel_2 = new JLabel("This framework uses LWJGL (www.lwjgl.org) and slick-util libraries.");
            lblNewLabel_2.setBounds(10, 92, 384, 14);
            lpAbout.add(lblNewLabel_2);
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
        txtFPS.setText( NumberFormat.getInstance().format(Rain.getFPS()) );
        //TODO: add sph particles
        txtParticles.setText( NumberFormat.getInstance().format(Rainstreaks.getMaxParticles()) );
    }
    
    public void close()
    {
        this.dispose();
        mDial = null;
        System.exit(0);
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
