package com.novamc.launcher;

import com.novamc.common.BuildInfo;
import com.novamc.launcher.config.LauncherSettings;
import com.novamc.launcher.version.InstallationManager;
import com.novamc.launcher.version.JavaRuntimeDetector;
import com.novamc.launcher.version.MinecraftVersion;
import com.novamc.launcher.version.VersionManager;
import com.novamc.updater.UpdateService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.nio.file.Path;
import java.util.List;

public final class LauncherMain {
    private static final Color BG = new Color(15, 17, 22), CARD = new Color(25, 28, 35), TEXT = new Color(238, 240, 245), MUTED = new Color(160, 166, 178), ACCENT = new Color(96, 125, 255);
    private static final VersionManager versionManager = new VersionManager();
    private static final Path DATA = Path.of(System.getProperty("user.home"), ".novamc");
    private static final LauncherSettings settings = LauncherSettings.load(DATA.resolve("launcher.json"));
    private static InstallationManager installationManager;

    public static void main(String[] args) { SwingUtilities.invokeLater(LauncherMain::create); }

    private static void create() {
        installationManager = new InstallationManager(resolveRoot(), versionManager);
        JFrame frame = new JFrame("NovaMC Client");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE); frame.setMinimumSize(new Dimension(1024, 650)); frame.setSize(1120, 720); frame.setLocationRelativeTo(null);
        JPanel root = new JPanel(new BorderLayout(18,18)); root.setBackground(BG); root.setBorder(new EmptyBorder(22,22,22,22));
        JLabel brand = label("NovaMC", 28, Font.BOLD, TEXT); JLabel sub = label("Original Minecraft client launcher • Phase 2", 13, Font.PLAIN, MUTED);
        JPanel header = new JPanel(new BorderLayout()); header.setOpaque(false); JPanel title = new JPanel(); title.setOpaque(false); title.setLayout(new BoxLayout(title, BoxLayout.Y_AXIS)); title.add(brand); title.add(Box.createVerticalStrut(4)); title.add(sub); header.add(title, BorderLayout.WEST);
        JButton settingsBtn = button("Settings"); header.add(settingsBtn, BorderLayout.EAST); root.add(header, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(18,18)); center.setOpaque(false);
        JPanel installCard = card(); installCard.setLayout(new BoxLayout(installCard, BoxLayout.Y_AXIS));
        installCard.add(label("VERSION MANAGER",12,Font.BOLD,MUTED)); installCard.add(Box.createVerticalStrut(16));
        JComboBox<MinecraftVersion> versions = new JComboBox<>(); versions.setRenderer(new DefaultListCellRenderer(){ public Component getListCellRendererComponent(JList<?> l,Object v,int i,boolean s,boolean f){ super.getListCellRendererComponent(l,v,i,s,f); if(v instanceof MinecraftVersion m)setText(m.id()+"  •  "+m.type()); return this; }}); versions.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34)); installCard.add(versions);
        installCard.add(Box.createVerticalStrut(12));
        JLabel rootLabel = label("Installation: " + installationManager.root(), 12, Font.PLAIN, MUTED); installCard.add(rootLabel);
        installCard.add(Box.createVerticalStrut(14));
        JPanel row = new JPanel(new GridLayout(1,3,8,0)); row.setOpaque(false); JButton refresh=button("Refresh"); JButton install=button("Install"); JButton repair=button("Repair"); row.add(refresh); row.add(install); row.add(repair); installCard.add(row);
        installCard.add(Box.createVerticalStrut(18)); JLabel progress = label("Ready",13,Font.BOLD,ACCENT); installCard.add(progress); installCard.add(Box.createVerticalGlue());
        JPanel runtime = card(); runtime.setLayout(new BoxLayout(runtime, BoxLayout.Y_AXIS)); runtime.add(label("RUNTIME",12,Font.BOLD,MUTED)); runtime.add(Box.createVerticalStrut(16));
        runtime.add(label("Java",12,Font.BOLD,MUTED)); JComboBox<String> javaBox = new JComboBox<>(); javaBox.setMaximumSize(new Dimension(Integer.MAX_VALUE,34)); runtime.add(javaBox);
        runtime.add(Box.createVerticalStrut(14)); runtime.add(label("RAM",12,Font.BOLD,MUTED)); JComboBox<Integer> ram = new JComboBox<>(new Integer[]{1024,1536,2048,3072,4096,6144,8192}); ram.setMaximumSize(new Dimension(Integer.MAX_VALUE,34)); ram.setSelectedItem(settings.ramMb); runtime.add(ram);
        runtime.add(Box.createVerticalStrut(14)); runtime.add(label("Selected instance",12,Font.BOLD,MUTED)); JLabel selected = label("None",14,Font.PLAIN,TEXT); runtime.add(selected); runtime.add(Box.createVerticalGlue());
        center.add(installCard, BorderLayout.CENTER); center.add(runtime, BorderLayout.EAST); runtime.setPreferredSize(new Dimension(330, 0)); root.add(center, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout()); footer.setOpaque(false); JLabel status = label("Phase 2: version manager ready",12,Font.PLAIN,MUTED); footer.add(status,BorderLayout.WEST); JButton play=button("PLAY"); footer.add(play,BorderLayout.EAST); root.add(footer,BorderLayout.SOUTH);

        for (JavaRuntimeDetector.JavaRuntime j : new JavaRuntimeDetector().detect()) javaBox.addItem(j.toString());
        if (javaBox.getItemCount()==0) javaBox.addItem("No Java runtime detected");
        refresh.addActionListener(e -> loadVersions(versions, progress, status));
        install.addActionListener(e -> installSelected(versions, progress, status, selected));
        repair.addActionListener(e -> repairSelected(versions, progress, status));
        versions.addActionListener(e -> { MinecraftVersion v=(MinecraftVersion)versions.getSelectedItem(); if(v!=null) selected.setText(installationManager.isInstalled(v.id()) ? v.id()+" (installed)" : v.id()+" (not installed)"); });
        ram.addActionListener(e -> { settings.ramMb=(Integer)ram.getSelectedItem(); saveSettings(); });
        settingsBtn.addActionListener(e -> JOptionPane.showMessageDialog(frame,"Data: "+DATA+"\nInstances: "+installationManager.root()+"\nRAM: "+settings.ramMb+" MB","NovaMC Settings",JOptionPane.INFORMATION_MESSAGE));
        play.addActionListener(e -> JOptionPane.showMessageDialog(frame,"Phase 2 does not launch Minecraft yet. Microsoft authentication and authenticated runtime launch are Phase 3.","NovaMC",JOptionPane.INFORMATION_MESSAGE));
        frame.setContentPane(root); frame.setVisible(true); loadVersions(versions,progress,status);
    }

    private static void loadVersions(JComboBox<MinecraftVersion> box,JLabel progress,JLabel status){ progress.setText("Fetching official version manifest..."); status.setText("Connecting to Mojang version metadata"); box.removeAllItems(); new SwingWorker<List<MinecraftVersion>,Void>(){ protected List<MinecraftVersion> doInBackground() throws Exception{return versionManager.fetchVersions();} protected void done(){try{for(MinecraftVersion v:get())box.addItem(v); progress.setText("Loaded "+box.getItemCount()+" versions"); status.setText("Version manifest ready"); if(settings.selectedVersion!=null&&!settings.selectedVersion.isBlank())for(int i=0;i<box.getItemCount();i++)if(box.getItemAt(i).id().equals(settings.selectedVersion))box.setSelectedIndex(i);}catch(Exception ex){progress.setText("Failed: "+ex.getMessage()); status.setText("Version manifest unavailable");}}}.execute(); }
    private static void installSelected(JComboBox<MinecraftVersion> box,JLabel progress,JLabel status,JLabel selected){ MinecraftVersion v=(MinecraftVersion)box.getSelectedItem(); if(v==null)return; progress.setText("Installing metadata for "+v.id()+"..."); new SwingWorker<Path,Void>(){protected Path doInBackground()throws Exception{return installationManager.install(v);}protected void done(){try{Path p=get();settings.selectedVersion=v.id();saveSettings();selected.setText(v.id()+" (installed)");progress.setText("Installed: "+p);status.setText("Installation ready");}catch(Exception ex){progress.setText("Install failed: "+ex.getMessage());status.setText("Installation failed");}}}.execute(); }
    private static void repairSelected(JComboBox<MinecraftVersion> box,JLabel progress,JLabel status){ MinecraftVersion v=(MinecraftVersion)box.getSelectedItem(); if(v==null)return; try{installationManager.repair(v.id());progress.setText("Repair complete: "+v.id());status.setText("Installation verified");}catch(Exception ex){progress.setText("Repair failed: "+ex.getMessage());}}
    private static Path resolveRoot(){String configured=settings.instanceRoot; return configured==null||configured.isBlank()?DATA.resolve("minecraft"):Path.of(configured);}
    private static void saveSettings(){try{settings.instanceRoot=installationManager.root().toString();settings.save(DATA.resolve("launcher.json"));}catch(Exception ignored){}}
    private static JPanel card(){JPanel p=new JPanel();p.setBackground(CARD);p.setBorder(new EmptyBorder(24,24,24,24));return p;}
    private static JLabel label(String t,int s,int st,Color c){JLabel l=new JLabel(t);l.setFont(new Font("SansSerif",st,s));l.setForeground(c);return l;}
    private static JButton button(String t){JButton b=new JButton(t);b.setFont(new Font("SansSerif",Font.BOLD,13));b.setForeground(Color.WHITE);b.setBackground(ACCENT);b.setFocusPainted(false);b.setBorder(BorderFactory.createEmptyBorder(10,18,10,18));return b;}
}
