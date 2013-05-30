package view;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import model.Card;
import model.Game;
import model.Hand;
import model.Player;
import client.HumanClient;

public class GamePanel extends JPanel
{
    private static final long serialVersionUID = 7889326310244251698L;

    private static final int FONT_SIZE = 16;
    private static final int FONT_HEIGHT = 20;

    private BufferedImage[][] CARD_IMAGES;
    private BufferedImage BIG_JOKER_IMAGE, SMALL_JOKER_IMAGE;
    private BufferedImage CARD_BACK_IMAGE;

    private HumanClient client;

    private Game game;

    public GamePanel(HumanClient client)
    {
        setBackground(Color.GREEN);
        this.client = client;
    }

    public void loadImages() throws IOException
    {
        CARD_IMAGES = new BufferedImage[13][4];
        for (int i = 0; i < CARD_IMAGES.length; i++)
            for (int j = 0; j < CARD_IMAGES[i].length; j++)
            {
                String filename = String.format("images/%c%s.gif",
                        "shdc".charAt(j),
                        "2 3 4 5 6 7 8 9 10 j q k 1".split(" ")[i]);
                CARD_IMAGES[i][j] = ImageIO.read(new File(filename));
            }
        BIG_JOKER_IMAGE = ImageIO.read(new File("images/jr.gif"));
        SMALL_JOKER_IMAGE = ImageIO.read(new File("images/jb.gif"));
        CARD_BACK_IMAGE = ImageIO.read(new File("images/b1fv.gif"));
    }

    public void setGame(Game game)
    {
        this.game = game;
    }

    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);

        if (game == null)
            return;

        int y;
        List<Player> players = game.getPlayers();

        g.setFont(new Font("Times New Roman", 0, FONT_SIZE));

        /* Draw game information */
        y = 0;
        g.drawString("Trump value: " + game.getTrumpValue(), 10,
                y += FONT_HEIGHT);
        g.drawString("Trump suit: "
                + (game.getTrumpSuit() == Card.SUIT.TRUMP ? '\u2668'
                        : (char) (game.getTrumpSuit().ordinal() + '\u2660')),
                10, y += FONT_HEIGHT);
        g.drawString("Starter: " + game.getMaster().name, 10, y += FONT_HEIGHT);

        /* Draw scores */
        y = 0;
        g.drawString("Scores", 900, y += FONT_HEIGHT);
        Map<Integer, Integer> playerScores = game.getPlayerScores();
        for (int playerID : playerScores.keySet())
            g.drawString(
                    findWithID(playerID, players).name + ": "
                            + Card.VALUE.values()[playerScores.get(playerID)],
                    800, y += FONT_HEIGHT);

        if (!game.started())
            return;

        /* Draw deck */
        if (game.canDrawFromDeck(game.getCurrentPlayer().ID))
            g.drawImage(CARD_BACK_IMAGE, 465, 350, null);

        /* Draw hands */
        int myIndex = players.indexOf(findWithID(client.myID(), players));
        for (int i = 0; i < players.size(); i++)
        {
            List<Card> cards = game.getSortedHandCards(players.get(i).ID);
            if (i != myIndex && cards.size() > 18)
                cards = cards.subList(0, 18);
            drawCards(cards, i - myIndex, 0.8, players.size(), i != myIndex, g);
        }

        /* Draw current trick */
    }

    private Player findWithID(int playerID, List<Player> players)
    {
        for (Player player : players)
            if (player.ID == playerID)
                return player;

        return null;
    }

    private void drawCards(List<Card> cards, int playerIndex,
            double percentage, int numPlayers, boolean faceDown, Graphics g)
    {
        /*
         * Draw cards of the given player. percentage refers to how far the
         * cards are placed from the center.
         */
        double angle = Math.PI / 2 - (2 * Math.PI / numPlayers * playerIndex);
        drawCards(cards, (int) (500 * (1 + percentage * Math.cos(angle))),
                (int) (400 * (1 + percentage * Math.sin(angle))), faceDown, g);
    }

    private void drawCards(List<Card> cards, int x, int y, boolean faceDown,
            Graphics g)
    {
        int cardDiff = faceDown ? Math.min(14, 100 / (cards.size() + 1)) : 14;
        int totalX = cardDiff * (cards.size() - 1) + 71;
        for (int i = 0; i < cards.size(); i++)
            drawCard(cards.get(i), x - totalX / 2 + cardDiff * i, y - 48, faceDown, g);
    }

    private void drawCard(Card card, int x, int y, boolean faceDown, Graphics g)
    {
        BufferedImage image;
        if (faceDown)
            image = CARD_BACK_IMAGE;
        else if (card.value == Card.VALUE.BIG_JOKER)
            image = BIG_JOKER_IMAGE;
        else if (card.value == Card.VALUE.SMALL_JOKER)
            image = SMALL_JOKER_IMAGE;
        else
            image = CARD_IMAGES[card.value.ordinal()][card.suit.ordinal()];
        g.drawImage(image, x, y, null);
    }
}
