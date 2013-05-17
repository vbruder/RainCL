package window;

import org.eclipse.swt.widgets.Composite;
import swing2swt.layout.BorderLayout;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Scale;
import swing2swt.layout.FlowLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.layout.GridData;
import org.eclipse.wb.swt.SWTResourceManager;

public class Settings extends Composite
{
    private Text text;
    private Text text_1;

    /**
     * Create the composite.
     * @param parent
     * @param style
     */
    public Settings(Composite parent, int style)
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
        
        Label label = new Label(composite, SWT.NONE);
        label.setText("FPS");
        new Label(composite, SWT.NONE);
        
        text_1 = new Text(composite, SWT.BORDER);
        text_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        new Label(composite, SWT.NONE);
        new Label(composite, SWT.NONE);
        new Label(composite, SWT.NONE);
        new Label(composite, SWT.NONE);
        new Label(composite, SWT.NONE);
        new Label(composite, SWT.NONE);
        new Label(composite, SWT.NONE);
        new Label(composite, SWT.NONE);
        
        Label label_1 = new Label(composite, SWT.NONE);
        label_1.setText("Particles");
        new Label(composite, SWT.NONE);
        
        text = new Text(composite, SWT.BORDER);
        text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        
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
        
        Label lblParticles = new Label(composite_1, SWT.NONE);
        lblParticles.setText("Particles");
        new Label(composite_1, SWT.NONE);
        
        Scale scale = new Scale(composite_1, SWT.NONE);
        GridData gd_scale = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_scale.widthHint = 270;
        scale.setLayoutData(gd_scale);
        
        TabItem tbtmLighting = new TabItem(tabFolder, SWT.NONE);
        tbtmLighting.setText("Lighting");
        
        Composite composite_2 = new Composite(tabFolder, SWT.NONE);
        tbtmLighting.setControl(composite_2);
        
        TabItem tbtmSystemInfo = new TabItem(tabFolder, SWT.NONE);
        tbtmSystemInfo.setText("System info");
        
        TabItem tbtmAbout = new TabItem(tabFolder, SWT.NONE);
        tbtmAbout.setText("About");

    }

    @Override
    protected void checkSubclass()
    {
        // Disable the check that prevents subclassing of SWT components
    }
}
