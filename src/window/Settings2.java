package window;

import org.eclipse.swt.widgets.Composite;
import swing2swt.layout.BorderLayout;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Scale;
import swing2swt.layout.FlowLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.layout.GridData;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.widgets.Button;

public class Settings2 extends Composite implements TimerListener
{
    private static Settings2 mDial;
    
    private Text txtParticles;
    private Text txtFPS;
    private Text text_2;
    private Text text_3;
    private Text text_4;
    private Text text_5;
    private Text text_6;

    /**
     * Create the composite.
     * @param parent
     * @param style
     */
    public Settings2(Composite parent, int style)
    {
        super(parent, style);
        setLayout(new BorderLayout(0, 0));
        
        Composite composite = new Composite(this, SWT.NONE);
        composite.setLayoutData(BorderLayout.CENTER);
        composite.setLayout(new GridLayout(13, false));
        new Label(composite, SWT.NONE);
        
        Label lblSettings = new Label(composite, SWT.NONE);
        lblSettings.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 2));
        lblSettings.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.BOLD));
        lblSettings.setText("Settings");
        new Label(composite, SWT.NONE);
        new Label(composite, SWT.NONE);
        new Label(composite, SWT.NONE);
        new Label(composite, SWT.NONE);
        new Label(composite, SWT.NONE);
        new Label(composite, SWT.NONE);
        new Label(composite, SWT.NONE);
        
        Label lblFPS = new Label(composite, SWT.NONE);
        lblFPS.setText("FPS");
        new Label(composite, SWT.NONE);
        
        txtFPS = new Text(composite, SWT.BORDER | SWT.READ_ONLY);
        txtFPS.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        new Label(composite, SWT.NONE);
        new Label(composite, SWT.NONE);
        new Label(composite, SWT.NONE);
        new Label(composite, SWT.NONE);
        new Label(composite, SWT.NONE);
        new Label(composite, SWT.NONE);
        new Label(composite, SWT.NONE);
        new Label(composite, SWT.NONE);
        
        Label lblParticles = new Label(composite, SWT.NONE);
        lblParticles.setText("Particles");
        new Label(composite, SWT.NONE);
        
        txtParticles = new Text(composite, SWT.BORDER | SWT.READ_ONLY);
        txtParticles.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        
        TabFolder tabFolder = new TabFolder(composite, SWT.NONE);
        GridData gd_tabFolder = new GridData(SWT.LEFT, SWT.BOTTOM, false, false, 13, 1);
        gd_tabFolder.widthHint = 433;
        gd_tabFolder.heightHint = 214;
        tabFolder.setLayoutData(gd_tabFolder);
        
        TabItem tbtmNewItem = new TabItem(tabFolder, SWT.NONE);
        tbtmNewItem.setText("General");
        
        Composite composite_1 = new Composite(tabFolder, SWT.NONE);
        tbtmNewItem.setControl(composite_1);
        composite_1.setLayout(new GridLayout(4, false));
        new Label(composite_1, SWT.NONE);
        new Label(composite_1, SWT.NONE);
        new Label(composite_1, SWT.NONE);
        new Label(composite_1, SWT.NONE);
        new Label(composite_1, SWT.NONE);
        
        Label lblRainfall = new Label(composite_1, SWT.NONE);
        lblRainfall.setText("Rainfall");
        new Label(composite_1, SWT.NONE);
        
        Scale scale = new Scale(composite_1, SWT.NONE);
        GridData gd_scale = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_scale.widthHint = 270;
        scale.setLayoutData(gd_scale);
        new Label(composite_1, SWT.NONE);
        new Label(composite_1, SWT.NONE);
        new Label(composite_1, SWT.NONE);
        new Label(composite_1, SWT.NONE);
        new Label(composite_1, SWT.NONE);
        new Label(composite_1, SWT.NONE);
        new Label(composite_1, SWT.NONE);
        
        Button btnSound = new Button(composite_1, SWT.CHECK);
        btnSound.setText("Sound");
        new Label(composite_1, SWT.NONE);
        new Label(composite_1, SWT.NONE);
        new Label(composite_1, SWT.NONE);
        
        Button btnWind = new Button(composite_1, SWT.CHECK);
        btnWind.setText("Wind");
        new Label(composite_1, SWT.NONE);
        new Label(composite_1, SWT.NONE);
        new Label(composite_1, SWT.NONE);
        
        Button btnFog = new Button(composite_1, SWT.CHECK);
        btnFog.setText("Fog");
        
        TabItem tbtmLighting = new TabItem(tabFolder, SWT.NONE);
        tbtmLighting.setText("Lighting");
        
        Composite composite_2 = new Composite(tabFolder, SWT.NONE);
        tbtmLighting.setControl(composite_2);
        
        TabItem tbtmSystemInfo = new TabItem(tabFolder, SWT.NONE);
        tbtmSystemInfo.setText("System info");
        
        Composite composite_3 = new Composite(tabFolder, SWT.NONE);
        tbtmSystemInfo.setControl(composite_3);
        composite_3.setLayout(new GridLayout(3, false));
        new Label(composite_3, SWT.NONE);
        new Label(composite_3, SWT.NONE);
        new Label(composite_3, SWT.NONE);
        new Label(composite_3, SWT.NONE);
        
        Label lblGraphics = new Label(composite_3, SWT.NONE);
        lblGraphics.setText("Graphics:");
        
        text_2 = new Text(composite_3, SWT.BORDER);
        text_2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        new Label(composite_3, SWT.NONE);
        
        Label lblDriver = new Label(composite_3, SWT.NONE);
        lblDriver.setText("Driver:");
        
        text_3 = new Text(composite_3, SWT.BORDER);
        text_3.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        new Label(composite_3, SWT.NONE);
        
        Label label_2 = new Label(composite_3, SWT.NONE);
        label_2.setText("OpenGL:");
        
        text_4 = new Text(composite_3, SWT.BORDER);
        text_4.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        new Label(composite_3, SWT.NONE);
        
        Label lblShadingLanguage = new Label(composite_3, SWT.NONE);
        lblShadingLanguage.setText("Shading Language:");
        
        text_5 = new Text(composite_3, SWT.BORDER);
        text_5.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        new Label(composite_3, SWT.NONE);
        
        Label lblOpencl = new Label(composite_3, SWT.NONE);
        lblOpencl.setText("OpenCL:");
        
        text_6 = new Text(composite_3, SWT.BORDER);
        text_6.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        
        TabItem tbtmAbout = new TabItem(tabFolder, SWT.NONE);
        tbtmAbout.setText("About");
        
        Composite composite_4 = new Composite(tabFolder, SWT.NONE);
        tbtmAbout.setControl(composite_4);
        
        Label lblcValentinBruder = new Label(composite_4, SWT.NONE);
        lblcValentinBruder.setBounds(10, 10, 413, 194);
        lblcValentinBruder.setText("RainCL - A rain simulation framework.\r\n\r\nVersion: 0.1\r\n\r\n(c) Valentin Bruder, Universit\u00E4t Osnabr\u00FCck, 2013\r\n\r\nThis framework uses LWJGL (www.lwjgl.org) and slick-util libraries.");

    }

    @Override
    protected void checkSubclass()
    {
        // Disable the check that prevents subclassing of SWT components
    }
    
    @Override
    public void updateTex()
    {
        txtFPS.setText("test");
    }
    
    public void close() {
        this.dispose();
        mDial = null;
    }
    
    /**
     * @brief returns instance of the object if not already existing (singleton pattern) 
     */
    public static Settings2 getInstance() {
        if(mDial != null) {
            //mDial.toFront();
            return mDial;
        }
        else {
            Shell shell = new Shell();
            mDial = new Settings2(shell, 0);
            return mDial;
        }
        
    }
    public static void destroyInstance() {
        if(mDial != null) {
            mDial.close();
        }
    }
}
