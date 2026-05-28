package br.com.cana.util;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import javax.swing.*;
import java.net.URL;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Projeto: Gestor de Partidas CANA
 * Utilitário de Design e Componentes Centralizados - ADS 2026
 * 
 * @author Gui
 */
public class ImagemUtil {

    // --- 1. DEFINIÇÕES DE CORES E FONTES (FIGMA) ---
    public static final Color COR_FUNDO = new Color(0xE9E4E4);
    public static final Color COR_AZUL_GRADIENTE = new Color(0x3E2BE5);
    public static final Color COR_VERMELHO_GRADIENTE = new Color(0xEF3333);
    public static final Font FONTE_BOTOES = new Font("SansSerif", Font.BOLD, 18);

    public static final String PATH_LOGO = "/images/logo_cana.png";
    public static final String PATH_ICONE = "/images/logo_cana.png";

    // --- 2. MÉTODOS DE ESTILIZAÇÃO E UTILITÁRIOS ---

    public static void ImagemBotoes(AbstractButton botao) {
        botao.setFont(FONTE_BOTOES);
        botao.setForeground(Color.WHITE);
        botao.setFocusPainted(false);
        botao.setBorderPainted(false);
        botao.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    public static void configurarIcone(java.awt.Window window) {
        try {
            // Mantém o uso da sua constante do projeto
            java.net.URL url = ImagemUtil.class.getResource(PATH_ICONE);

            if (url != null) {
                Image imgOriginal = new javax.swing.ImageIcon(url).getImage();
                java.util.List<Image> listaIconesHD = new java.util.ArrayList<>();

                // Resoluções padrão que o sistema operacional exige para cada canto da tela
                int[] tamanhos = { 16, 24, 32, 48, 64, 128, 256 };

                for (int tam : tamanhos) {
                    java.awt.image.BufferedImage bi = new java.awt.image.BufferedImage(tam, tam,
                            java.awt.image.BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g2 = bi.createGraphics();

                    // 🛠️ FILTROS DE ALTA DEFINIÇÃO (Bicúbico + Antialiasing)
                    g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                            java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,
                            java.awt.RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                    g2.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING,
                            java.awt.RenderingHints.VALUE_RENDER_QUALITY);

                    g2.drawImage(imgOriginal, 0, 0, tam, tam, null);
                    g2.dispose();

                    listaIconesHD.add(bi);
                }

                // Define a coleção de imagens. A Window seleciona a resolução ideal
                // automaticamente
                window.setIconImages(listaIconesHD);
            } else {
                System.err.println("Aviso: Arquivo de imagem não encontrado em " + PATH_ICONE);
            }
        } catch (Exception e) {
            System.err.println("Erro ao carregar ícone em HD: " + e.getMessage());
        }
    }

    public static Image getImagem(String path) {
        try {
            URL url = ImagemUtil.class.getResource(path);
            if (url != null) {
                return new ImageIcon(url).getImage();
            }
        } catch (Exception e) {
            System.err.println("Erro ao carregar imagem: " + path);
        }
        return null;
    }

    public static ImageIcon getIconeRedimensionado(String path, int largura, int altura) {
        Image img = getImagem(path);
        if (img != null) {
            Image novaImg = img.getScaledInstance(largura, altura, Image.SCALE_SMOOTH);
            return new ImageIcon(novaImg);
        }
        return null;
    }

    public static void desenharImagemCentralizada(Graphics g, Image img, int larguraPainel, int alturaPainel) {
        if (img == null)
            return;

        int imgLargura = img.getWidth(null);
        int imgAltura = img.getHeight(null);

        double escala = Math.min((double) larguraPainel / imgLargura, (double) alturaPainel / imgAltura);
        int novaLargura = (int) (imgLargura * escala);
        int novaAltura = (int) (imgAltura * escala);

        int x = (larguraPainel - novaLargura) / 2;
        int y = (alturaPainel - novaAltura) / 2;

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(img, x, y, novaLargura, novaAltura, null);
    }

    // --- 3. COMPONENTES CUSTOMIZADOS (CLASSES INTERNAS) ---

    public static class BotaoGradienteCANA extends JButton {
        public BotaoGradienteCANA(String texto) {
            super(texto);
            ImagemUtil.ImagemBotoes(this);
            setContentAreaFilled(false);
            setPreferredSize(new Dimension(300, 80));
            setMaximumSize(new Dimension(300, 80));
            setAlignmentX(Component.CENTER_ALIGNMENT);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            float[] distribuicao = { 0.25f, 0.75f };
            Color[] cores = { COR_AZUL_GRADIENTE, COR_VERMELHO_GRADIENTE };
            LinearGradientPaint lgp = new LinearGradientPaint(0, 0, getWidth(), 0, distribuicao, cores);

            g2.setPaint(lgp);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 25, 25));

            super.paintComponent(g);
            g2.dispose();
        }
    }

    public static class LogoPainelCANA extends JPanel {
        private Image img = ImagemUtil.getImagem(ImagemUtil.PATH_LOGO);

        public LogoPainelCANA(int largura, int altura) {
            setOpaque(false);
            setPreferredSize(new Dimension(largura, altura));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (img != null) {
                ImagemUtil.desenharImagemCentralizada(g, img, getWidth(), getHeight());
            }
        }
    }

    /**
     * Renderizador customizado para JTable.
     * Remove os bugs de linhas grossas do Swing e aplica uma linha fina elegante na
     * base.
     */
    public static class TabelaLinhaLimpaRenderer extends javax.swing.table.DefaultTableCellRenderer {
        @Override
        public java.awt.Component getTableCellRendererComponent(javax.swing.JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {

            // Invoca o comportamento padrão para carregar os textos
            javax.swing.JLabel celula = (javax.swing.JLabel) super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);

            // Alinhamento visual: Nome da partida alinhado à esquerda, o resto centralizado
            if (column == 1) {
                celula.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
                celula.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 10, 0, 0)); // Margem esquerda pro texto
                                                                                            // não colar na linha
            } else {
                celula.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            }

            // Definição de cores de acordo com a identidade do CANA
            if (!isSelected) {
                celula.setBackground(java.awt.Color.WHITE);
                celula.setForeground(new java.awt.Color(0x333333)); // Grafite elegante
            } else {
                // Mantém o azul padrão do Swing quando o usuário clica na linha
                celula.setBackground(table.getSelectionBackground());
                celula.setForeground(table.getSelectionForeground());
            }
            
            return celula;
        }
    }
    
    /**
     * Tira um print de um JPanel específico e salva em um arquivo temporário.
     */
    /**
     * Tira um print de um JPanel específico em Alta Resolução (Superamostragem 2x)
     * e salva em um arquivo temporário.
     */
    public static File tirarPrintPainel(JPanel painel, String nomeArquivo) {
        try {
            // Aumenta a escala para 2x (superamostragem) para garantir que o texto e os detalhes fiquem nítidos mesmo em telas de alta resolução
            int escala = 2; 

            // Cria a imagem em branco já com o tamanho multiplicado
            BufferedImage imagem = new BufferedImage(
                painel.getWidth() * escala, 
                painel.getHeight() * escala, 
                BufferedImage.TYPE_INT_RGB
            );
            
            Graphics2D g2d = imagem.createGraphics();
            
            // 🌟 ATIVA A SUAVIZAÇÃO (Anti-Aliasing) para as fontes e bordas não ficarem serrilhadas
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Aplica a escala para que o painel seja desenhado grandão e preencha a imagem toda
            g2d.scale(escala, escala);

            // Pede para o painel se "desenhar"
            painel.printAll(g2d);
            g2d.dispose();

            // Salva o arquivo temporário
            File arquivo = new File(System.getProperty("java.io.tmpdir"), nomeArquivo + ".png");
            ImageIO.write(imagem, "png", arquivo);

            return arquivo;

        } catch (Exception e) {
            System.err.println("❌ Erro ao capturar print da tela: " + e.getMessage());
            return null;
        }
    }

    /**
     * Pega um arquivo de imagem e joga na Área de Transferência (Clipboard) do sistema operacional,
     * pronto para o usuário dar um "Ctrl + V" no WhatsApp ou Paint.
     */
    public static void copiarParaClipboard(File arquivoImagem) {
        try {
            final java.awt.Image image = ImageIO.read(arquivoImagem);
            
            java.awt.datatransfer.Transferable transferable = new java.awt.datatransfer.Transferable() {
                @Override
                public java.awt.datatransfer.DataFlavor[] getTransferDataFlavors() {
                    return new java.awt.datatransfer.DataFlavor[] { java.awt.datatransfer.DataFlavor.imageFlavor };
                }

                @Override
                public boolean isDataFlavorSupported(java.awt.datatransfer.DataFlavor flavor) {
                    return java.awt.datatransfer.DataFlavor.imageFlavor.equals(flavor);
                }

                @Override
                public Object getTransferData(java.awt.datatransfer.DataFlavor flavor) throws java.awt.datatransfer.UnsupportedFlavorException {
                    if (isDataFlavorSupported(flavor)) {
                        return image;
                    }
                    throw new java.awt.datatransfer.UnsupportedFlavorException(flavor);
                }
            };
            
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(transferable, null);
        } catch (Exception e) {
            System.err.println("❌ Erro ao copiar imagem para o Clipboard: " + e.getMessage());
        }
    }

    public static String converterTokensParaHtml(String tokens) {
        if (tokens == null || tokens.trim().isEmpty())
            return "";

        StringBuilder sb = new StringBuilder();
        sb.append("<table border='0' cellpadding='0' cellspacing='3'><tr>");

        String[] partes = tokens.split(" / ", -1);
        for (int i = 0; i < partes.length; i++) {
            String[] subTokens = partes[i].trim().split(" ");
            for (String t : subTokens) {
                if (t.equals("⚽")) {
                    sb.append("<td valign='middle'>⚽</td>");
                } else if (t.equals("⚽(C)")) {
                    sb.append("<td valign='middle'>⚽(C)</td>");
                } else if (t.equals("🟨")) {
                    sb.append(
                            "<td bgcolor='#FFCC00' width='8' height='12' valign='middle'><font size='1'>&nbsp;</font></td>");
                } else if (t.equals("🟥")) {
                    sb.append(
                            "<td bgcolor='#8B0000' width='8' height='12' valign='middle'><font size='1'>&nbsp;</font></td>");
                }
            }
            if (i < partes.length - 1) {
                sb.append("<td valign='middle' style='color:black; font-weight:bold;'>&nbsp;/&nbsp;</td>");
            }
        }

        sb.append("</tr></table>");
        return sb.toString();
    }
}