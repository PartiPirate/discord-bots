package fr.partipirate.discord.bots.congressus.commands.congressus;

import java.awt.Color;

import fr.partipirate.discord.bots.congressus.commands.ICommand;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

public class PPCommand extends ACongressusCommand implements ICommand {

	@Override
	public String getKeyWord() {
		return "pp";
	}

	@Override
	public boolean canDoCommand(User user, Guild guild) {
		return true;
	}

	@Override
	public void doCommand(User user, MessageChannel channel, Guild guild, String[] commandParts) {
		if (commandParts.length == 1) {
			channel.sendMessage("Veuillez indiquez un sujet").complete();
		}
		else if (commandParts[1].equalsIgnoreCase("statuts")) {
			EmbedBuilder eb = new EmbedBuilder();				
			
			eb.setTitle("Les statuts du Parti Pirate", "https://wiki.partipirate.fr/Statuts");
			eb.setColor(new Color(0x8814CC));
			eb.setDescription("La page wiki des statuts du Parti Pirate");

			channel.sendMessage(eb.build()).complete();
		}
		else if (commandParts[1].equalsIgnoreCase("ri")) {
			EmbedBuilder eb = new EmbedBuilder();				
			
			eb.setTitle("Le règlement intérieur du Parti Pirate", "https://wiki.partipirate.org/R%C3%A8glement_int%C3%A9rieur");
			eb.setColor(new Color(0x8814CC));
			eb.setDescription("La page wiki du règlement intérieur du Parti Pirate");

			channel.sendMessage(eb.build()).complete();
		}
		else if (commandParts[1].equalsIgnoreCase("code")) {
			EmbedBuilder eb = new EmbedBuilder();				

			eb.setColor(new Color(0x8814CC));

			if (commandParts.length == 2) {
				eb.setTitle("Le code du Parti Pirate", "https://partipirate.org/le-code-des-pirates/");
				eb.setDescription("La base de la base à laquelle les Pirates consentent");
			}
			else {
				try {
					int codePoint = Integer.parseInt(commandParts[2]);
					
					String[] point = getPoint(codePoint);
					eb.setTitle(point[0]);
					eb.setDescription(point[1]);
				}
				catch (NumberFormatException e) {
					boolean found = false;
					
					for(int i = 1; i <= 14; i++) {
						String[] point = getPoint(i);
						
						if (point[0].toLowerCase().contains(commandParts[2].toLowerCase()) || point[1].toLowerCase().contains(commandParts[2].toLowerCase())) {
							eb.setTitle(point[0]);
							eb.setDescription(point[1]);

							found = true;

							break;
						}
					}

					if (!found) {
						channel.sendMessage("Je ne trouve rien de correspondant, désolé :smirk_cat:").complete();
						return;
					}
				}
			}

			channel.sendMessage(eb.build()).complete();
		}
	}

	private String[] getPoint(int codePoint) {
		switch(codePoint) {
			case 1:
				String[] point1 =  {"I – Les Pirates sont libres.", "Nous, Pirates, chérissons la liberté, l’indépendance, l’autonomie et refusons toute forme d’obédience aveugle.\n" + 
						"Nous affirmons le droit à nous informer nous-mêmes et à choisir notre propre destin.\n" + 
						"Nous assumons la responsabilité qu’induit la liberté."};
				return point1;
			case 2:
				String[] point2 =  {"II – Les Pirates respectent la vie privée.", "Nous, Pirates, protégeons la vie privée. Nous combattons l’obsession croissante de surveillance car elle empêche le libre développement de l’individu. Une société libre et démocratique est impossible sans un espace de liberté hors-surveillance."};
				return point2;
			case 3:
				String[] point3 =  {"III – Les Pirates ont l’esprit critique.", "Nous, Pirates, encourageons la créativité et la curiosité. Nous ne nous satisfaisons pas du statu quo. Nous défions les systèmes, traquons les failles et les corrigeons. Nous apprenons de nos erreurs."};
				return point3;
			case 4:
				String[] point4 =  {"IV – Les Pirates sont environnementalistes.", "Nous, Pirates, luttons contre la destruction de l’environnement et toute forme de capitalisation des ressources. Nous militons pour la pérennité de la nature et de ce qui la compose. Nous n’acceptons aucun brevet sur le vivant."};
				return point4;
			case 5:
				String[] point5 =  {"V – Les Pirates sont avides de connaissance.", "L’accès à l’information, à l’éducation et au savoir doit être illimité. Nous, Pirates, soutenons la culture libre et le logiciel libre."};
				return point5;
			case 6:
				String[] point6 =  {"VI – Les Pirates sont solidaires.", "Nous, Pirates, respectons la dignité humaine et rejetons la peine de mort. Nous nous engageons pour une société solidaire défendant une conception de la politique faite d’objectivité et d’équité."};
				return point6;
			case 7:
				String[] point7 =  {"VII – Les Pirates sont cosmopolites.", "Nous, Pirates, faisons partie d’un mouvement mondial. Nous nous appuyons sur l’opportunité qu’offre Internet de penser et d’agir par-delà les frontières."};
				return point7;
			case 8:
				String[] point8 =  {"VIII – Les Pirates sont équitables.", "Nous, Pirates, luttons pour l’égalité entre les personnes, sans considération de genre, de couleur de peau, d’âge, d’orientation sexuelle, de niveau d’études, de statut, d’origine ou de handicap. Nous militons pour la liberté de s’épanouir."};
				return point8;
			case 9:
				String[] point9 =  {"IX – Les Pirates rassemblent.", "Nous, Pirates, ne prétendons pas avoir la solution à tous les problèmes. Nous pensons que réfléchir collectivement est nécessaire, nous invitons donc tout le monde à s’engager politiquement, à contribuer à partir de ses connaissances, expériences et perspectives. Nous saluons les contributions qui sortent des sentiers battus."};
				return point9;
			case 10:
				String[] point10 =  {"X – Les Pirates relient.", "Nous, Pirates constatons que tant les bonheurs individuels que communs se fondent sur les liens que nous tissons avec nous mêmes, les autres, la société, la nature et le monde. Par la numérisation et Internet, la moitié de la population mondiale est connectée en un réseau horizontal et décentralisé. Cette conscience collective transforme le monde."};
				return point10;
			case 11:
				String[] point11 =  {"Les Pirates font confiance.", "Nous, Pirates avons confiance en nous et osons faire confiance aux autres. Nous croyons en la collaboration et contribuons aux communs ainsi qu’aux projets collectifs. Nous portons un regard bienveillant sur la vie en communauté."};
				return point11;
			case 12:
				String[] point12 =  {"XII – Les Pirates font preuve d’audace.", "Nous, Pirates, n’attendons pas que des solutions viennent à nous mais nous organisons par nous-mêmes pour répondre aux problèmes que nous rencontrons. Nous croyons en la force des mouvements collaboratifs et horizontaux."};
				return point12;
			case 13:
				String[] point13 =  {"XIII – Les Pirates sont hétéroclites.", "Nous, Pirates, voulons apprendre de nos différences pour progresser et dépasser la notion de polarité. Nous ne croyons pas au traditionnel bipartisme.\n" + 
						"\n" + 
						"Tout le monde peut être Pirate."};
				return point13;
			default:
				String[] pointDefault =  {"", ""};
				return pointDefault;
		}
	}

	@Override
	public String getCommandHelp() {
		return "Donne url ou information suivant la sous requete demandée [statuts | ri | code [n°|mot]].";
	}
}