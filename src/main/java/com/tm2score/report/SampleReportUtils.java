/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.report;

import com.itextpdf.text.Image;
import com.tm2builder.sim.xml.InterviewQuestionObj;
import com.tm2builder.sim.xml.SimJ;
import com.tm2score.custom.coretest.ITextUtils;
import com.tm2score.custom.coretest2.CT3Constants;
import com.tm2score.event.ScoreCategoryType;
import com.tm2score.event.ScoreColorSchemeType;
import com.tm2score.global.Constants;
import com.tm2score.global.RuntimeConstants;
import com.tm2score.interview.InterviewQuestion;
import com.tm2score.score.TextAndTitle;
import com.tm2score.service.LogService;
import com.tm2score.sim.NonCompetencyItemType;
import com.tm2score.sim.SimCompetencyClass;
import com.tm2score.util.MessageFactory;
import com.tm2score.util.UrlEncodingUtils;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 *
 * @author Mike
 */
public class SampleReportUtils {

    static Random random;
    
    public static String companyName = "ABC Industries";
    
    private static String[] firstNames = new String[]{"Aaren","Aarika","Abagael","Abagail","Abbe","Abbey","Abbi","Abbie","Abby","Abbye","Abigael","Abigail","Abigale","Abra","Ada","Adah","Adaline","Adan","Adara","Adda","Addi","Addia","Addie","Addy","Adel","Adela","Adelaida","Adelaide","Adele","Adelheid","Adelice","Adelina","Adelind","Adeline","Adella","Adelle","Adena","Adey","Adi","Adiana","Adina","Adora","Adore","Adoree","Adorne","Adrea","Adria","Adriaens","Adrian","Adriana","Adriane","Adrianna","Adrianne","Adriena","Adrienne","Aeriel","Aeriela","Aeriell","Afton","Ag","Agace","Agata","Agatha","Agathe","Aggi","Aggie","Aggy","Agna","Agnella","Agnes","Agnese","Agnesse","Agneta","Agnola","Agretha","Aida","Aidan","Aigneis","Aila","Aile","Ailee","Aileen","Ailene","Ailey","Aili","Ailina","Ailis","Ailsun","Ailyn","Aime","Aimee","Aimil","Aindrea","Ainslee","Ainsley","Ainslie","Ajay","Alaine","Alameda","Alana","Alanah","Alane","Alanna","Alayne","Alberta","Albertina","Albertine","Albina","Alecia","Aleda","Aleece","Aleen","Alejandra","Alejandrina","Alena","Alene","Alessandra","Aleta","Alethea","Alex","Alexa","Alexandra","Alexandrina","Alexi","Alexia","Alexina","Alexine","Alexis","Alfi","Alfie","Alfreda","Alfy","Ali","Alia","Alica","Alice","Alicea","Alicia","Alida","Alidia","Alie","Alika","Alikee","Alina","Aline","Alis","Alisa","Alisha","Alison","Alissa","Alisun","Alix","Aliza","Alla","Alleen","Allegra","Allene","Alli","Allianora","Allie","Allina","Allis","Allison","Allissa","Allix","Allsun","Allx","Ally","Allyce","Allyn","Allys","Allyson","Alma","Almeda","Almeria","Almeta","Almira","Almire","Aloise","Aloisia","Aloysia","Alta","Althea","Alvera","Alverta","Alvina","Alvinia","Alvira","Alyce","Alyda","Alys","Alysa","Alyse","Alysia","Alyson","Alyss","Alyssa","Amabel","Amabelle","Amalea","Amalee","Amaleta","Amalia","Amalie","Amalita","Amalle","Amanda","Amandi","Amandie","Amandy","Amara","Amargo","Amata","Amber","Amberly","Ambur","Ame","Amelia","Amelie","Amelina","Ameline","Amelita","Ami","Amie","Amii","Amil","Amitie","Amity","Ammamaria","Amy","Amye","Ana","Anabal","Anabel","Anabella","Anabelle","Analiese","Analise","Anallese","Anallise","Anastasia","Anastasie","Anastassia","Anatola","Andee","Andeee","Anderea","Andi","Andie","Andra","Andrea","Andreana","Andree","Andrei","Andria","Andriana","Andriette","Andromache","Andy","Anestassia","Anet","Anett","Anetta","Anette","Ange","Angel","Angela","Angele","Angelia","Angelica","Angelika","Angelina","Angeline","Angelique","Angelita","Angelle","Angie","Angil","Angy","Ania","Anica","Anissa","Anita","Anitra","Anjanette","Anjela","Ann","Ann-Marie","Anna","Anna-Diana","Anna-Diane","Anna-Maria","Annabal","Annabel","Annabela","Annabell","Annabella","Annabelle","Annadiana","Annadiane","Annalee","Annaliese","Annalise","Annamaria","Annamarie","Anne","Anne-Corinne","Anne-Marie","Annecorinne","Anneliese","Annelise","Annemarie","Annetta","Annette","Anni","Annice","Annie","Annis","Annissa","Annmaria","Annmarie","Annnora","Annora","Anny","Anselma","Ansley","Anstice","Anthe","Anthea","Anthia","Anthiathia","Antoinette","Antonella","Antonetta","Antonia","Antonie","Antonietta","Antonina","Anya","Appolonia","April","Aprilette","Ara","Arabel","Arabela","Arabele","Arabella","Arabelle","Arda","Ardath","Ardeen","Ardelia","Ardelis","Ardella","Ardelle","Arden","Ardene","Ardenia","Ardine","Ardis","Ardisj","Ardith","Ardra","Ardyce","Ardys","Ardyth","Aretha","Ariadne","Ariana","Aridatha","Ariel","Ariela","Ariella","Arielle","Arlana","Arlee","Arleen","Arlen","Arlena","Arlene","Arleta","Arlette","Arleyne","Arlie","Arliene","Arlina","Arlinda","Arline","Arluene","Arly","Arlyn","Arlyne","Aryn","Ashely","Ashia","Ashien","Ashil","Ashla","Ashlan","Ashlee","Ashleigh","Ashlen","Ashley","Ashli","Ashlie","Ashly","Asia","Astra","Astrid","Astrix","Atalanta","Athena","Athene","Atlanta","Atlante","Auberta","Aubine","Aubree","Aubrette","Aubrey","Aubrie","Aubry","Audi","Audie","Audra","Audre","Audrey","Audrie","Audry","Audrye","Audy","Augusta","Auguste","Augustina","Augustine","Aundrea","Aura","Aurea","Aurel","Aurelea","Aurelia","Aurelie","Auria","Aurie","Aurilia","Aurlie","Auroora","Aurora","Aurore","Austin","Austina","Austine","Ava","Aveline","Averil","Averyl","Avie","Avis","Aviva","Avivah","Avril","Avrit","Ayn","Bab","Babara","Babb","Babbette","Babbie","Babette","Babita","Babs","Bambi","Bambie","Bamby","Barb","Barbabra","Barbara","Barbara-Anne","Barbaraanne","Barbe","Barbee","Barbette","Barbey","Barbi","Barbie","Barbra","Barby","Bari","Barrie","Barry","Basia","Bathsheba","Batsheva","Bea","Beatrice","Beatrisa","Beatrix","Beatriz","Bebe","Becca","Becka","Becki","Beckie","Becky","Bee","Beilul","Beitris","Bekki","Bel","Belia","Belicia","Belinda","Belita","Bell","Bella","Bellanca","Belle","Bellina","Belva","Belvia","Bendite","Benedetta","Benedicta","Benedikta","Benetta","Benita","Benni","Bennie","Benny","Benoite","Berenice","Beret","Berget","Berna","Bernadene","Bernadette","Bernadina","Bernadine","Bernardina","Bernardine","Bernelle","Bernete","Bernetta","Bernette","Berni","Bernice","Bernie","Bernita","Berny","Berri","Berrie","Berry","Bert","Berta","Berte","Bertha","Berthe","Berti","Bertie","Bertina","Bertine","Berty","Beryl","Beryle","Bess","Bessie","Bessy","Beth","Bethanne","Bethany","Bethena","Bethina","Betsey","Betsy","Betta","Bette","Bette-Ann","Betteann","Betteanne","Betti","Bettina","Bettine","Betty","Bettye","Beulah","Bev","Beverie","Beverlee","Beverley","Beverlie","Beverly","Bevvy","Bianca","Bianka","Bibbie","Bibby","Bibbye","Bibi","Biddie","Biddy","Bidget","Bili","Bill","Billi","Billie","Billy","Billye","Binni","Binnie","Binny","Bird","Birdie","Birgit","Birgitta","Blair","Blaire","Blake","Blakelee","Blakeley","Blanca","Blanch","Blancha","Blanche","Blinni","Blinnie","Blinny","Bliss","Blisse","Blithe","Blondell","Blondelle","Blondie","Blondy","Blythe","Bobbe","Bobbee","Bobbette","Bobbi","Bobbie","Bobby","Bobbye","Bobette","Bobina","Bobine","Bobinette","Bonita","Bonnee","Bonni","Bonnibelle","Bonnie","Bonny","Brana","Brandais","Brande","Brandea","Brandi","Brandice","Brandie","Brandise","Brandy","Breanne","Brear","Bree","Breena","Bren","Brena","Brenda","Brenn","Brenna","Brett","Bria","Briana","Brianna","Brianne","Bride","Bridget","Bridgette","Bridie","Brier","Brietta","Brigid","Brigida","Brigit","Brigitta","Brigitte","Brina","Briney","Brinn","Brinna","Briny","Brit","Brita","Britney","Britni","Britt","Britta","Brittan","Brittaney","Brittani","Brittany","Britte","Britteny","Brittne","Brittney","Brittni","Brook","Brooke","Brooks","Brunhilda","Brunhilde","Bryana","Bryn","Bryna","Brynn","Brynna","Brynne","Buffy","Bunni","Bunnie","Bunny","Cacilia","Cacilie","Cahra","Cairistiona","Caitlin","Caitrin","Cal","Calida","Calla","Calley","Calli","Callida","Callie","Cally","Calypso","Cam","Camala","Vonny","Vyky","Wallie","Wallis","Walliw","Wally","Waly","Wanda","Wandie","Wandis","Waneta","Wanids","Wenda","Wendeline","Wendi","Wendie","Wendy","Wendye","Wenona","Wenonah","Whitney","Wileen","Wilhelmina","Wilhelmine","Wilie","Willa","Willabella","Willamina","Willetta","Willette","Willi","Willie","Willow","Willy","Willyt","Wilma","Wilmette","Wilona","Wilone","Wilow","Windy","Wini","Winifred","Winna","Winnah","Winne","Winni","Winnie","Winnifred","Winny","Winona","Winonah","Wren","Wrennie","Wylma","Wynn","Wynne","Wynnie","Wynny","Xaviera","Xena","Xenia","Xylia","Xylina","Yalonda","Yasmeen","Yasmin","Yelena","Yetta","Yettie","Yetty","Yevette","Ynes","Ynez","Yoko","Yolanda","Yolande","Yolane","Yolanthe","Yoshi","Yoshiko","Yovonnda","Ysabel","Yvette","Yvonne","Zabrina","Zahara","Zandra","Zaneta","Zara","Zarah","Zaria","Zarla","Zea","Zelda","Zelma","Zena","Zenia","Zia","Zilvia","Zita","Zitella","Zoe","Zola","Zonda","Zondra","Zonnya","Zora","Zorah","Zorana","Zorina","Zorine","Zsa Zsa","Zsazsa","Zulema","Zuzana"};

    private static String[] lastNames = new String[] { "Dallis","Dallman","Dallon","Daloris","Dalpe","Dalston","Dalt","Dalton","Dalury","Daly","Dam","Damal","Damalas","Damales","Damali","Damalis","Damalus","Damara","Damaris","Damarra","Dambro","Dame","Damek","Damian","Damiani","Damiano","Damick","Damicke","Damien","Damita","Damle","Damon","Damour","Dan","Dana","Danae","Danaher","Danais","Danas","Danby","Danczyk","Dane","Danell","Danella","Danelle","Danete","Danette","Daney","Danforth","Dang","Dani","Dania","Daniala","Danialah","Danica","Danice","Danie","Daniel","Daniela","Daniele","Daniell","Daniella","Danielle","Daniels","Danielson","Danieu","Danika","Danila","Danit","Danita","Daniyal","Dann","Danna","Dannel","Danni","Dannica","Dannie","Dannon","Danny","Dannye","Dante","Danuloff","Danya","Danyelle","Danyette","Danyluk","Danzig","Danziger","Dao","Daph","Daphene","Daphie","Daphna","Daphne","Dar","Dara","Darach","Darb","Darbee","Darbie","Darby","Darce","Darcee","Darcey","Darci","Darcia","Darcie","Darcy","Darda","Dardani","Dare","Dareece","Dareen","Darees","Darell","Darelle","Daren","Dari","Daria","Darian","Darice","Darill","Darin","Dario","Darius","Darken","Darla","Darleen","Darlene","Darline","Darlleen","Darmit","Darn","Darnall","Darnell","Daron","Darooge","Darra","Darrel","Darrell","Darrelle","Darren","Darrey","Darrick","Darrill","Darrin","Darrow","Darryl","Darryn","Darsey","Darsie","Dart","Darton","Darwen","Darwin","Darya","Daryl","Daryle","Daryn","Dash","Dasha","Dasi","Dasie","Dasteel","Dasya","Datha","Datnow","Daub","Daugherty","Daughtry","Daukas","Daune","Dav","Dave","Daveda","Daveen","Daven","Davena","Davenport","Daveta","Davey","David","Davida","Davidde","Davide","Davidoff","Davidson","Davie","Davies","Davilman","Davin","Davina","Davine","Davis","Davison","Davita","Davon","Davy","Dawes","Dawkins","Dawn","Dawna","Dawson","Day","Daye","Dayle","Dayna","Ddene","De","De Witt","Deach","Deacon","Deadman","Dean","Deana","Deane","Deaner","Deanna","Deanne","Dearborn","Dearden","Dearman","Dearr","Deb","Debarath","Debbee","Debbi","Debbie","Debbra","Debby","Debee","Debera","Debi","Debor","Debora","Deborah","Deborath","Debra","Decamp","Decato","Decca","December","Decima","Deck","Decker","Deckert","Declan","Dede","Deden","Dedie","Dedra","Dedric","Dedrick","Dee","Dee Dee","DeeAnn","Deeann","Deeanne","Deedee","Deegan","Deena","Deenya","Deer","Deerdre","Deering","Deery","Deeyn","Defant","Dehlia","Dehnel","Deibel","Deidre","Deina","Deirdra","Deirdre","Dekeles","Dekow","Del","Dela","Delacourt","Delaine","Delainey","Delamare","Deland","Delaney","Delanie","Delano","Delanos","Delanty","Delaryd","Delastre","Delbert","Delcina","Delcine","Delfeena","Delfine","Delgado","Delia","Delija","Delila","Delilah","Delinda","Delisle","Dell","Della","Delle","Dellora","Delly","Delmar","Delmer","Delmor","Delmore","Delogu","Delora","Delorenzo","Delores","Deloria","Deloris","Delos","Delp","Delphina","Delphine","Delphinia","Delsman","Delwin","Delwyn","Demaggio","Demakis","Demaria","Demb","Demeter","Demetra","Demetre","Demetri","Demetria","Demetris","Demetrius","Demeyer","Deming","Demitria","Demmer","Demmy","Demodena","Demona","Demott","Demp","Dempsey","Dempster","Dempstor","Demy","Den","Dena","Denae","Denbrook","Denby","Dene","Deni","Denice","Denie","Denis","Denise","Denison","Denman","Denn","Denna","Dennard","Dennet","Dennett","Denney","Denni","Dennie","Dennis","Dennison","Denny","Denoting","Dent","Denten","Denton","Denver","Deny","Denys","Denyse","Denzil","Deonne","Depoliti","Deppy","Der","Deragon","Derayne","Derby","Dercy","Derek","Derian","Derick","Derina","Derinna","Derk","Derman","Dermot","Dermott","Derna","Deron","Deroo","Derr","Derrek","Derrick","Derriey","Derrik","Derril","Derron","Derry","Derte","Derward","Derwin","Derwon","Derwood","Deryl","Derzon","Des","Desai","Desberg","Descombes","Desdamona","Desdamonna","Desdee","Desdemona","Desi","Desimone","Desirae","Desirea","Desireah","Desiree","Desiri","Desma","Desmond","Desmund","Dessma","Desta","Deste","Destinee","Deth","Dett","Detta","Dettmer","Deuno","Deutsch","Dev","Deva","Devan","Devaney","Dever","Devi","Devin","Devina","Devine","Devinna","Devinne","Devitt","Devland","Devlen","Devlin","Devol","Devon","Devona","Devondra","Devonna","Devonne","Devora","Devy","Dew","Dewain","Dewar","Dewayne","Dewees","Dewey","Dewhirst","Dewhurst","Dewie","Dewitt","Dex","Dexter","Dey","Dhar","Dhiman","Dhiren","Dhruv","Dhu","Dhumma","Di","Diahann","Diamante","Diamond","Dian","Diana","Diandra","Diandre","Diane","Diane-Marie","Dianemarie","Diann","Dianna","Dianne","Diannne","Diantha","Dianthe","Diao","Diarmid","Diarmit","Diarmuid","Diaz","Dib","Diba","Dibb","Dibbell","Dibbrun","Dibri","Dibrin","Dibru","Dich","Dichy","Dick","Dickens","Dickenson","Dickerson","Dickey","Dickie","Dickinson","Dickman","Dicks","Dickson","Dicky","Didi","Didier","Dido","Dieball","Diego","Diehl","Diella","Dielle","Dielu","Diena","Dierdre","Dierolf","Diet","Dieter","Dieterich","Dietrich","Dietsche","Dietz","Dikmen","Dilan","Diley","Dilisio","Dilks","Dill","Dillie","Dillon","Dilly","Dimitri","Dimitris","Dimitry","Dimmick","Dimond","Dimphia","Dina","Dinah","Dinan","Dincolo","Dine","Dinerman","Dinesh","Dinin","Dinnage","Dinnie","Dinny","Dino","Dinsdale","Dinse","Dinsmore","Diogenes","Dion","Dione","Dionis","Dionisio","Dionne","Dionysus","Dippold","Dira","Dirk","Disario","Disharoon","Disini","Diskin","Diskson","Disraeli","Dita","Ditmore","Ditter","Dittman","Dituri","Ditzel","Diver","Divine","Dix","Dixie","Dixil","Dixon","Dmitri","Dniren","Doak","Doane","Dobb","Dobbins","Doble","Dobrinsky","Dobson","Docia","Docila","Docile","Docilla","Docilu","Dodd","Dodds","Dode","Dodge","Dodi","Dodie","Dodson","Dodwell","Dody","Doe","Doehne","Doelling","Doerrer","Doersten","Doggett","Dogs","Doherty","Doi","Doig","Dola","Dolan","Dole","Doley","Dolf","Dolhenty","Doll","Dollar","Dolley","Dolli","Dollie","Dolloff","Dolly","Dolora","Dolores","Dolorita","Doloritas","Dolph","Dolphin","Dom","Domash","Dombrowski","Domel","Domela","Domella","Domenech","Domenic","Domenico","Domeniga","Domineca","Dominga","Domingo","Domini","Dominic","Dominica","Dominick","Dominik","Dominique","Dominus","Dominy","Domonic","Domph","Don","Dona","Donadee","Donaghue","Donahoe","Donahue","Donal","Donald","Donaldson","Donall","Donalt","Donata","Donatelli","Donaugh","Donavon","Donegan","Donela","Donell","Donella","Donelle","Donelson","Donelu","Doner","Donetta","Dong","Donia","Donica","Donielle","Donn","Donna","Donnamarie","Donnell","Donnelly","Donnenfeld","Donni","Donnie","Donny","Donoghue","Donoho","Donohue","Donough","Donovan","Doolittle","Doone","Dopp","Dora","Doralia","Doralin","Doralyn","Doralynn","Doralynne","Doran","Dorca","Dorcas","Dorcea","Dorcia","Dorcus","Dorcy","Dore","Doreen","Dorelia","Dorella","Dorelle","Dorena","Dorene","Doretta","Dorette","Dorey","Dorfman","Dori","Doria","Dorian","Dorice","Dorie","Dorin","Dorina","Dorinda","Dorine","Dorion","Doris","Dorisa","Dorise","Dorison","Dorita","Dorkas","Dorkus","Dorlisa","Dorman","Dorn","Doro","Dorolice","Dorolisa","Dorotea","Doroteya","Dorothea","Dorothee","Dorothi","Dorothy","Dorr","Dorran","Dorree","Dorren","Dorri","Dorrie","Dorris","Dorry","Dorsey","Dorsman","Dorsy","Dorthea","Dorthy","Dorweiler","Dorwin","Dory","Doscher","Dosh","Dosi","Dosia","Doss","Dot","Doti","Dotson","Dott","Dotti","Dottie","Dotty","Doty","Doubler","Doug","Dougal","Dougald","Dougall","Dougherty","Doughman","Doughty","Dougie","Douglas","Douglass","Dougy","Douty","Douville","Dov","Dove","Dovev","Dow","Dowd","Dowdell","Dowell","Dowlen","Dowling","Down","Downall","Downe","Downes","Downey","Downing","Downs","Dowski","Dowzall","Doxia","Doy","Doykos","Doyle","Drabeck","Dragelin","Dragon","Dragone","Dragoon","Drain","Drais","Drake","Drandell","Drape","Draper","Dray","Dre","Dream","Dreda","Dreddy","Dredi","Dreeda","Dreher","Dremann","Drescher","Dressel","Dressler","Drew","Drewett","Drews","Drexler","Dreyer","Dric","Drice","Drida","Dripps","Driscoll","Driskill","Drisko","Drislane","Drobman","Drogin","Drolet","Drona","Dronski","Drooff","Dru","Druce","Druci","Drucie","Drucill","Drucilla","Drucy","Drud","Drue","Drugge","Drugi","Drummond","Drus","Drusi","Drusie","Drusilla","Drusus","Drusy","Dry","Dryden","Drye","Dryfoos","DuBois","Duane","Duarte","Duax","Dubenko","Dublin","Ducan","Duck","Dud","Dudden","Dudley","Duer","Duester","Duff","Duffie","Duffy","Dugaid","Dugald","Dugan","Dugas","Duggan","Duhl","Duke","Dukey","Dukie","Duky","Dulce","Dulcea","Dulci","Dulcia","Dulciana","Dulcie","Dulcine","Dulcinea","Dulcle","Dulcy","Duleba","Dulla","Dulsea","Duma","Dumah","Dumanian","Dumas","Dumm","Dumond","Dun","Dunaville","Dunc","Duncan","Dunham","Dunkin","Dunlavy","Dunn","Dunning","Dunseath","Dunson","Dunstan","Dunston","Dunton","Duntson","Duong","Dupaix","Dupin","Dupre","Dupuis","Dupuy","Duquette","Dur","Durand","Durant","Durante","Durarte","Durer","Durgy","Durham","Durkee","Durkin","Durman","Durnan","Durning","Durno","Durr","Durrace","Durrell","Durrett","Durst","Durstin","Durston","Durtschi","Durward","Durware","Durwin","Durwood","Durwyn","Dusa","Dusen","Dust","Dustan","Duster","Dustie","Dustin","Dustman","Duston","Dusty","Dusza","Dutch","Dutchman","Duthie","Duval","Duvall","Duwalt","Duwe","Duyne","Dwain","Dwaine","Dwan","Dwane","Dwayne","Dweck","Dwight","Dwinnell","Dworman","Dwyer","Dyal","Dyan","Dyana","Dyane","Dyann","Dyanna","Dyanne","Dyche","Dyer","Dygal","Dygall","Dygert","Dyke","Dyl","Dylan","Dylana","Dylane","Dymoke","Dympha","Dymphia","Dyna","Dynah","Dysart","Dyson","Dyun","Dzoba","Eachelle","Eachern","Eada","Eade","Eadie","Eadith","Eadmund","Eads","Eadwina","Eadwine","Eagle","Eal","Ealasaid","Eamon","Eanore","Earl","Earla","Earle","Earleen","Earlene","Earley","Earlie","Early","Eartha","Earvin","East","Easter","Eastlake","Eastman","Easton","Eaton","Eatton","Eaves","Eb","Eba","Ebarta","Ebba","Ebbarta","Ebberta","Ebbie","Ebby","Eben","Ebeneser","Ebenezer","Eberhard","Eberhart","Eberle","Eberly","Ebert","Eberta","Eberto","Ebner","Ebneter","Eboh","Ebonee","Ebony","Ebsen","Echikson","Echo","Eckardt","Eckart","Eckblad","Eckel","Eckhardt","Eckmann","Econah","Ed","Eda","Edan","Edana","Edbert","Edd","Edda","Eddana","Eddi","Eddie","Eddina","Eddra","Eddy","Ede","Edea","Edee","Edeline","Edelman","Edelson","Edelstein","Edelsten","Eden","Edette","Edgar","Edgard","Edgardo","Edge","Edgell","Edgerton","Edholm","Edi","Edie","Edik","Edin","Edina","Edison","Edita","Edith","Editha","Edithe","Ediva","Edla","Edlin","Edlun","Edlyn","Edmanda","Edme","Edmea","Edmead","Edmee","Edmon","Edmond","Edmonda","Edmondo","Edmonds","Edmund","Edmunda","Edna","Edny","Edora","Edouard","Edra","Edrea","Edrei","Edric","Edrick","Edris","Edrock","Edroi","Edsel","Edson","Eduard","Eduardo","Eduino","Edva","Edvard","Edveh","Edward","Edwards","Edwin","Edwina","Edwine","Edwyna","Edy","Edyth","Edythe","Effie","Effy","Efram","Efrem","Efren","Efron","Efthim","Egan","Egarton","Egbert","Egerton","Eggett","Eggleston","Egide","Egidio","Egidius","Egin","Eglanteen","Eglantine","Egon","Egor","Egwan","Egwin","Ehling","Ehlke","Ehman","Ehr","Ehrenberg","Ehrlich","Ehrman","Ehrsam","Ehud","Ehudd","Eichman","Eidson","Eiger","Eileen","Eilis","Eimile","Einberger","Einhorn","Eipper","Eirena","Eirene","Eisele","Eisen","Eisenberg","Eisenhart","Eisenstark","Eiser","Eisinger","Eisler","Eiten","Ekaterina","El","Ela","Elah","Elaina","Elaine","Elana","Elane","Elata","Elatia","Elayne","Elazaro","Elbart","Elberfeld","Elbert","Elberta","Elbertina","Elbertine","Elboa","Elbring","Elburr","Elburt","Elconin","Elda","Elden","Elder","Eldin","Eldon","Eldora","Eldorado","Eldoree","Eldoria","Eldred","Eldreda","Eldredge","Eldreeda","Eldrid","Eldrida","Eldridge","Eldwen","Eldwin","Eldwon","Eldwun","Eleanor","Eleanora","Eleanore","Eleazar","Electra","Eleen","Elena","Elene","Eleni","Elenore","Eleonora","Eleonore","Eleph","Elephus","Elery","Elexa","Elfie","Elfont","Elfreda","Elfrida","Elfrieda","Elfstan","Elga","Elgar","Eli","Elia","Eliades","Elianora","Elianore","Elias","Eliason","Eliath","Eliathan","Eliathas","Elicia","Elidad","Elie","Eliezer","Eliga","Elihu","Elijah","Elinor","Elinore","Eliot","Eliott","Elisa","Elisabet","Elisabeth","Elisabetta","Elise","Elisee","Eliseo","Elish","Elisha","Elison","Elissa","Elita","Eliza","Elizabet","Elizabeth","Elka","Elke","Elkin","Ella","Elladine","Ellan","Ellard","Ellary","Ellata","Elle","Ellen","Ellene","Ellerd","Ellerey","Ellersick","Ellery","Ellett","Ellette","Ellga","Elli","Ellicott","Ellie","Ellinger","Ellingston","Elliot","Elliott","Ellis","Ellison","Ellissa","Ellita","Ellmyer","Ellon","Ellora","Ellord","Ellswerth","Ellsworth","Ellwood","Elly","Ellyn","Ellynn","Elma","Elmajian","Elmaleh","Elman","Elmer","Elmina","Elmira","Elmo","Elmore","Elna","Elnar","Elnora","Elnore","Elo","Elodea","Elodia","Elodie","Eloisa","Eloise","Elon","Elonore","Elora","Elreath","Elrod","Elroy","Els","Elsa","Elsbeth","Else","Elset","Elsey","Elsi","Elsie","Elsinore","Elson","Elspet","Elspeth","Elstan","Elston","Elsworth","Elsy","Elton","Elum","Elurd","Elva","Elvah","Elvera","Elvia","Elvie","Elvin","Elvina","Elvira","Elvis","Elvyn","Elwaine","Elwee","Elwin","Elwina","Elwira","Elwood","Elwyn","Ely","Elyn","Elyse","Elysee","Elysha","Elysia","Elyssa","Em","Ema","Emad","Emalee","Emalia","Emanuel","Emanuela","Emanuele","Emarie","Embry","Emee","Emelda","Emelen","Emelia","Emelin","Emelina","Emeline","Emelita","Emelun","Emelyne","Emera","Emerald","Emeric","Emerick","Emersen","Emerson","Emery","Emie","Emil","Emile","Emilee","Emili","Emilia","Emilie","Emiline","Emilio","Emily","Emina","Emlen","Emlin","Emlyn","Emlynn","Emlynne","Emma","Emmalee","Emmaline","Emmalyn","Emmalynn","Emmalynne","Emmanuel","Emmeline","Emmer","Emmeram","Emmerich","Emmerie","Emmery","Emmet","Emmett","Emmey","Emmi","Emmie","Emmit","Emmons","Emmott","Emmuela","Emmy","Emmye","Emogene","Emory","Emrich","Emsmus","Emyle","Emylee","Enalda","Encrata","Encratia","Encratis","End","Ender","Endo","Endor","Endora","Endres","Enenstein","Eng","Engdahl","Engeddi","Engedi","Engedus","Engel","Engelbert","Engelhart","Engen","Engenia","England","Engle","Englebert","Engleman","Englis","English","Engracia","Engud","Engvall","Enid","Ennis","Eno","Enoch","Enos","Enrica","Enrichetta","Enrico","Enrika","Enrique","Enriqueta","Ensign","Ensoll","Entwistle","Enyedy","Eoin","Eolanda","Eolande","Eph","Ephraim","Ephram","Ephrayim","Ephrem","Epifano","Epner","Epp","Epperson","Eppes","Eppie","Epps","Epstein","Er","Eradis","Eran","Eras","Erasme","Erasmo","Erasmus","Erastatus","Eraste","Erastes","Erastus","Erb","Erbe","Erbes","Erda","Erdah","Erdda","Erde","Erdei","Erdman","Erdrich","Erek","Erelia","Erena","Erfert","Ergener","Erhard","Erhart","Eri","Eric","Erica","Erich","Ericha","Erick","Ericka","Ericksen","Erickson","Erida","Erie","Eriha","Erik","Erika","Erikson","Erin","Erina","Erine","Erinn","Erinna","Erkan","Erl","Erland","Erlandson","Erle","Erleena","Erlene","Erlewine","Erlin","Erlina","Erline","Erlinna","Erlond","Erma","Ermanno","Erme","Ermeena","Ermengarde","Ermentrude","Ermey","Ermin","Ermina","Ermine","Erminia","Erminie","Erminna","Ern","Erna","Ernald","Ernaldus","Ernaline","Ernest","Ernesta","Ernestine","Ernesto","Ernestus","Ernie","Ernst","Erny","Errecart","Errick","Errol","Erroll","Erskine","Ertha","Erund","Erv","ErvIn","Ervin","Ervine","Erving","Erwin","Eryn","Esau","Esbensen","Esbenshade","Esch","Esdras","Eshelman","Eshman","Eskil","Eskill","Esma","Esmaria","Esme","Esmeralda","Esmerelda","Esmerolda","Esmond","Espy","Esra","Essa","Essam","Essex","Essie","Essinger","Essy","Esta","Estas","Esteban","Estel","Estele","Estell","Estella","Estelle","Esten","Ester","Estes","Estevan","Estey","Esther","Estis","Estrella","Estrellita","Estren","Estrin","Estus","Eta","Etam","Etan","Etana","Etem","Ethan","Ethban","Ethben","Ethbin","Ethbinium","Ethbun","Ethe","Ethel","Ethelbert","Ethelda","Ethelin","Ethelind","Ethelinda","Etheline","Ethelred","Ethelstan","Ethelyn","Ethyl","Etienne","Etka","Etoile","Etom","Etra","Etrem","Etta","Ettari","Etti","Ettie","Ettinger","Ettore","Etty","Etz","Eudo","Eudoca","Eudocia","Eudora","Eudosia","Eudoxia","Euell","Eugen","Eugene","Eugenia","Eugenides","Eugenie","Eugenio","Eugenius","Eugeniusz","Eugenle","Eugine","Euh","Eula","Eulalee","Eulalia","Eulaliah","Eulalie","Eulau","Eunice","Eupheemia","Euphemia","Euphemiah","Euphemie","Euridice","Eurydice","Eusebio","Eustace","Eustache","Eustacia","Eustashe","Eustasius","Eustatius","Eustazio","Eustis","Euton","Ev","Eva","Evadne","Evadnee","Evaleen","Evalyn","Evan","Evander","Evangelia","Evangelin","Evangelina","Evangeline","Evangelist","Evania","Evanne","Evannia","Evans","Evante","Evanthe","Evars","Eve","Eveleen","Evelin","Evelina","Eveline","Evelinn","Evelunn","Evelyn","Even","Everara","Everard","Evered","Everest","Everett","Everick","Everrs","Evers","Eversole","Everson","Evetta","Evette","Evey","Evie","Evin","Evita","Evonne","Evoy","Evslin","Evvie","Evvy","Evy","Evyn","Ewald","Ewall","Ewan","Eward","Ewart","Ewell","Ewen","Ewens","Ewer","Ewold","Eyde","Eydie","Eyeleen","Eyla","Ez","Ezana","Ezar","Ezara","Ezaria","Ezarra","Ezarras","Ezechiel","Ezekiel","Ezequiel","Eziechiele","Ezmeralda","Ezra","Ezri","Ezzo","Fabe","Faber","Fabi","Fabian","Fabiano","Fabien","Fabio","Fabiola","Fabiolas","Fablan","Fabozzi","Fabri","Fabria","Fabriane","Fabrianna","Fabrianne","Fabrice","Fabrienne","Fabrin","Fabron","Fabyola","Fachan","Fachanan","Fachini","Fadden","Faden","Fadil","Fadiman","Fae","Fagaly","Fagan","Fagen","Fagin","Fahey","Fahland","Fahy","Fai","Faina","Fair","Fairbanks","Faires","Fairfax","Fairfield","Fairleigh","Fairley","Fairlie","Fairman","Fairweather","Faith","Fakieh","Falcone","Falconer","Falda","Faletti","Faline","Falito","Falk","Falkner","Fallon","Faludi","Falzetta","Fan","Fanchan","Fanchet","Fanchette","Fanchie","Fanchon","Fancie","Fancy","Fanechka","Fanestil","Fang","Fania","Fanni","Fannie","Fanning","Fanny","Fantasia","Fante","Fanya","Far","Fara","Farah","Farand","Farant","Farhi","Fari","Faria","Farica","Farika","Fariss","Farkas","Farl","Farland","Farlay","Farlee","Farleigh","Farley","Farlie","Farly","Farman","Farmann","Farmelo","Farmer","Farnham","Farnsworth","Farny","Faro","Farr","Farra","Farrah","Farrand","Farrar","Farrel","Farrell","Farrica","Farrington","Farris","Farrish","Farrison","Farro","Farron","Farrow","Faruq","Farver","Farwell","Fasano","Faso","Fassold","Fast","Fasta","Fasto","Fates","Fatima","Fatimah","Fatma","Fattal","Faubert","Faubion","Fauch","Faucher","Faulkner","Fauman","Faun","Faunia","Faunie","Faus","Faust","Fausta","Faustena","Faustina","Faustine","Faustus","Fauver","Faux","Favata","Favian","Favianus","Favien","Favin","Favrot","Fawcett","Fawcette","Fawn","Fawna","Fawne","Fawnia","Fax","Faxan","Faxen","Faxon","Faxun","Fay","Faydra","Faye","Fayette","Fayina","Fayola","Fayre","Fayth","Faythe","Fazeli","Fe","Featherstone","February","Fechter","Fedak","Federica","Federico","Fedirko","Fedora","Fee","Feeley","Feeney","Feer","Feigin","Feil","Fein","Feinberg","Feingold","Feinleib","Feinstein","Feld","Felder","Feldman","Feldstein","Feldt","Felecia","Feledy","Felic","Felicdad","Felice","Felicia","Felicidad","Felicie","Felicio","Felicity","Felicle","Felike","Feliks","Felipa","Felipe","Felise","Felisha","Felita","Felix","Feliza","Felizio","Fellner","Fellows","Felske","Felt","Felten","Feltie","Felton","Felty","Fem","Femi","Femmine","Fen","Fendig","Fenelia","Fenella","Fenn","Fennell","Fennelly","Fenner","Fennessy","Fennie","Fenny","Fenton","Fenwick","Feodor","Feodora","Feodore","Feola","Ferd","Ferde","Ferdie","Ferdinana","Ferdinand","Ferdinanda","Ferdinande","Ferdy","Fergus","Ferguson","Feriga","Ferino","Fermin","Fern","Ferna","Fernald","Fernand","Fernanda","Fernande","Fernandes","Fernandez","Fernandina","Fernando","Fernas","Ferne","Ferneau","Fernyak","Ferrand","Ferreby","Ferree","Ferrel","Ferrell","Ferren","Ferretti","Ferri","Ferrick","Ferrigno","Ferris","Ferriter","Ferro","Ferullo","Ferwerda","Festa","Festatus","Festus","Feucht","Feune","Fevre","Fey","Fi","Fia","Fiann","Fianna","Fidel","Fidela","Fidelas","Fidele","Fidelia","Fidelio","Fidelis","Fidelity","Fidellas","Fidellia","Fiden","Fidole","Fiedler","Fiedling","Field","Fielding","Fields","Fiertz","Fiester","Fife","Fifi","Fifine","Figge","Figone","Figueroa","Filbert","Filberte","Filberto","Filemon","Files","Filia","Filiano","Filide","Filip","Filipe","Filippa","Filippo","Fillander","Fillbert","Fillender","Filler","Fillian","Filmer","Filmore","Filomena","Fin","Fina","Finbar","Finbur","Findlay","Findley","Fine","Fineberg","Finegan","Finella","Fineman","Finer","Fini","Fink","Finkelstein","Finlay","Finley","Finn","Finnegan","Finnie","Finnigan","Finny","Finstad","Finzer","Fiona","Fionna","Fionnula","Fiora","Fiore","Fiorenza","Fiorenze","Firestone","Firman","Firmin","Firooc","Fisch","Fischer","Fish","Fishback","Fishbein","Fisher","Fishman","Fisk","Fiske","Fisken","Fitting","Fitton","Fitts","Fitz","Fitzger","Fitzgerald","Fitzhugh","Fitzpatrick","Fitzsimmons","Flagler","Flaherty","Flam","Flan","Flanagan","Flanders","Flanigan","Flann","Flanna","Flannery","Flatto","Flavia","Flavian","Flavio","Flavius","Fleck","Fleda","Fleece","Fleeman","Fleeta","Fleischer","Fleisher","Fleisig","Flem","Fleming","Flemings","Flemming","Flessel","Fleta","Fletch","Fletcher","Fleur","Fleurette","Flieger","Flight","Flin","Flinn","Flint","Flip","Flita","Flo","Floeter","Flor","Flora","Florance","Flore","Florella","Florence","Florencia","Florentia","Florenza","Florette","Flori","Floria","Florian","Florida","Floridia","Florie","Florin","Florina","Florinda","Florine","Florio","Floris","Floro","Florri","Florrie","Florry","Flory","Flosi","Floss","Flosser","Flossi","Flossie","Flossy","Flower","Flowers","Floyd","Flss","Flyn","Flynn","Foah","Fogarty","Fogel","Fogg","Fokos","Folberth","Foley","Folger","Follansbee","Follmer","Folly","Folsom","Fonda","Fondea","Fong","Fons","Fonseca","Fonsie","Fontana","Fontes","Fonville","Fonz","Fonzie","Foote","Forbes","Forcier","Ford","Fording","Forelli","Forest","Forester","Forkey","Forland","Forlini","Formenti","Formica","Fornof","Forras","Forrer","Forrest","Forrester","Forsta","Forster","Forsyth","Forta","Fortier","Fortin","Fortna","Fortuna","Fortunato","Fortune","Fortunia","Fortunio","Fortunna","Forward","Foscalina","Fosdick","Foskett","Fosque","Foss","Foster","Fotina","Fotinas","Fougere","Foulk","Four","Foushee","Fowkes","Fowle","Fowler","Fox","Foy","Fraase","Fradin","Frager","Frame","Fran","France","Francene","Frances","Francesca","Francesco","Franchot","Franci","Francie","Francine","Francis","Francisca","Franciscka","Francisco","Franciska","Franciskus","Franck","Francklin","Francklyn","Franckot","Francois","Francoise","Francyne","Franek","Frangos","Frank","Frankel","Frankhouse","Frankie","Franklin","Franklyn","Franky","Franni","Frannie","Franny","Frans","Fransen","Fransis","Fransisco","Frants","Frantz","Franz","Franza","Franzen","Franzoni","Frasch","Frasco","Fraser","Frasier","Frasquito","Fraya","Frayda","Frayne","Fraze","Frazer","Frazier","Frear","Freberg","Frech","Frechette","Fred","Freda","Freddi","Freddie","Freddy","Fredek","Fredel","Fredela","Fredelia","Fredella","Fredenburg","Frederic","Frederica","Frederich","Frederick","Fredericka","Frederico","Frederigo","Frederik","Frederiksen","Frederique","Fredette","Fredi","Fredia","Fredie","Fredkin","Fredra","Fredric","Fredrick","Fredrika","Free","Freeborn","Freed","Freedman","Freeland","Freeman","Freemon","Fregger","Freida","Freiman","Fremont","French","Frendel","Frentz","Frere","Frerichs","Fretwell","Freud","Freudberg","Frey","Freya","Freyah","Freytag","Frick","Fricke","Frida","Friday","Fridell","Fridlund","Fried","Frieda","Friedberg","Friede","Frieder","Friederike","Friedland","Friedlander","Friedly","Friedman","Friedrich","Friedrick","Friend","Frierson","Fries","Frisse","Frissell","Fritts","Fritz","Fritze","Fritzie","Fritzsche","Frodeen","Frodi","Frodin","Frodina","Frodine","Froehlich","Froemming","Froh","Frohman","Frohne","Frolick","Froma","Fromma","Fronia","Fronnia","Fronniah","Frost","Fruin","Frulla","Frum","Fruma","Fry","Fryd","Frydman","Frye","Frymire","Fu","Fuchs","Fugate","Fugazy","Fugere","Fuhrman","Fujio","Ful","Fulbert","Fulbright","Fulcher","Fuld","Fulks","Fuller","Fullerton","Fulmer","Fulmis","Fulton","Fulvi","Fulvia","Fulviah","Funch","Funda","Funk","Furey","Furgeson","Furie","Furiya","Furlani","Furlong","Furmark","Furnary","Furr","Furtek","Fusco","Gaal","Gabbert","Gabbey","Gabbi","Gabbie","Gabby","Gabe","Gabel","Gabey","Gabi","Gabie","Gable","Gabler","Gabor","Gabriel","Gabriela","Gabriele","Gabriell","Gabriella","Gabrielle","Gabrielli","Gabriellia","Gabriello","Gabrielson","Gabrila","Gaby","Gad","Gaddi","Gader","Gadmann","Gadmon","Gae","Gael","Gaelan","Gaeta","Gage","Gagliano","Gagne","Gagnon","Gahan","Gahl","Gaidano","Gaige","Gail","Gaile","Gaillard","Gainer","Gainor","Gaiser","Gaither","Gaivn","Gal","Gala","Galan","Galang","Galanti","Galasyn","Galatea","Galateah","Galatia","Gale","Galen","Galer","Galina","Galitea","Gall","Gallager","Gallagher","Gallard","Gallenz","Galliett","Galligan","Galloway","Gally","Galvan","Galven","Galvin","Gamages","Gamal","Gamali","Gamaliel","Gambell","Gamber","Gambrell","Gambrill","Gamin","Gan","Ganiats","Ganley","Gannes","Gannie","Gannon","Ganny","Gans","Gant","Gapin","Gar","Garald","Garate","Garaway","Garbe","Garber","Garbers","Garceau","Garcia","Garcon","Gard","Garda","Gardal","Gardas","Gardel","Gardell","Gardener","Gardia","Gardie","Gardiner","Gardner","Gardol","Gardy","Gare","Garek","Gareri","Gareth","Garett","Garey","Garfield","Garfinkel","Gargan","Garges","Garibald","Garibold","Garibull","Gariepy","Garik","Garin","Garlaand","Garlan","Garland","Garlanda","Garlen","Garlinda","Garling","Garmaise","Garneau","Garner","Garnes","Garnet","Garnett","Garnette","Garold","Garrard","Garratt","Garrek","Garret","Garreth","Garretson","Garrett","Garrick","Garrik","Garris","Garrison","Garrity","Garrot","Garrott","Garry","Garson","Garth","Garv","Garvey","Garvin","Garvy","Garwin","Garwood","Gary","Garzon","Gascony","Gaskill","Gaskin","Gaskins","Gaspar","Gaspard","Gasparo","Gasper","Gasperoni","Gass","Gasser","Gassman","Gastineau","Gaston","Gates","Gathard","Gathers","Gati","Gatian","Gatias","Gaudet","Gaudette","Gaughan","Gaul","Gauldin","Gaulin","Gault","Gaultiero","Gauntlett","Gausman","Gaut","Gautea","Gauthier","Gautier","Gautious","Gav","Gavan","Gaven","Gavette","Gavin","Gavini","Gavra","Gavrah","Gavriella","Gavrielle","Gavrila","Gavrilla","Gaw","Gawain","Gawen","Gawlas","Gay","Gaye","Gayel","Gayelord","Gayl","Gayla","Gayle","Gayleen","Gaylene","Gayler","Gaylor","Gaylord","Gayn","Gayner","Gaynor","Gazo","Gazzo","Geaghan","Gean","Geanine","Gearalt","Gearard","Gearhart","Gebelein","Gebhardt","Gebler","Geddes","Gee","Geehan","Geer","Geerts","Geesey","Gefell","Gefen","Geffner","Gehlbach","Gehman","Geibel","Geier","Geiger","Geilich","Geis","Geiss","Geithner","Gelasias","Gelasius","Gelb","Geldens","Gelhar","Geller","Gellman","Gelman","Gelya","Gemina","Gemini","Geminian","Geminius","Gemma","Gemmell","Gemoets","Gemperle","Gen","Gena","Genaro","Gene","Genesa","Genesia","Genet","Geneva","Genevieve","Genevra","Genia","Genie","Genisia","Genna","Gennaro","Genni","Gennie","Gennifer","Genny","Geno","Genovera","Gensler","Gensmer","Gent","Gentes","Gentilis","Gentille","Gentry","Genvieve","Geof","Geoff","Geoffrey","Geoffry","Georas","Geordie","Georg","George","Georgeanna","Georgeanne","Georgena","Georges","Georgeta","Georgetta","Georgette","Georgi","Georgia","Georgiana","Georgianna","Georgianne","Georgie","Georgina","Georgine","Georglana","Georgy","Ger","Geraint","Gerald","Geralda","Geraldina","Geraldine","Gerard","Gerardo","Geraud","Gerbold","Gerda","Gerdeen","Gerdi","Gerdy","Gere","Gerek","Gereld","Gereron","Gerfen","Gerge","Gerger","Gerhan","Gerhard","Gerhardine","Gerhardt","Geri","Gerianna","Gerianne","Gerick","Gerik","Gerita","Gerius","Gerkman","Gerlac","Gerladina","Germain","Germaine","German","Germana","Germann","Germano","Germaun","Germayne","Germin","Gernhard","Gerome","Gerrald","Gerrard","Gerri","Gerrie","Gerrilee","Gerrit","Gerry","Gersham","Gershom","Gershon","Gerson","Gerstein","Gerstner","Gert","Gerta","Gerti","Gertie","Gertrud","Gertruda","Gertrude","Gertrudis","Gerty","Gervais","Gervase","Gery","Gesner","Gessner","Getraer","Getter","Gettings","Gewirtz","Ghassan","Gherardi","Gherardo","Gherlein","Ghiselin","Giacamo","Giacinta","Giacobo","Giacomo","Giacopo","Giaimo","Giamo","Gian","Giana","Gianina","Gianna","Gianni","Giannini","Giarla","Giavani","Gib","Gibb","Gibbeon","Gibbie","Gibbon","Gibbons","Gibbs","Gibby","Gibe","Gibeon","Gibert","Gibrian","Gibson","Gibun","Giddings","Gide","Gideon","Giefer","Gies","Giesecke","Giess","Giesser","Giff","Giffard","Giffer","Gifferd","Giffie","Gifford","Giffy","Gigi","Giglio","Gignac","Giguere","Gil","Gilba","Gilbart","Gilbert","Gilberta","Gilberte","Gilbertina","Gilbertine","Gilberto","Gilbertson","Gilboa","Gilburt","Gilbye","Gilchrist","Gilcrest","Gilda","Gildas","Gildea","Gilder","Gildus","Gile","Gilead","Gilemette","Giles","Gilford","Gilges","Giliana","Giliane","Gill","Gillan","Gillead","Gilleod","Gilles","Gillespie","Gillett","Gilletta","Gillette","Gilli","Gilliam","Gillian","Gillie","Gilliette","Gilligan","Gillman","Gillmore","Gilly","Gilman","Gilmer","Gilmore","Gilmour","Gilpin","Gilroy","Gilson","Giltzow","Gilud","Gilus","Gimble","Gimpel","Gina","Ginder","Gine","Ginelle","Ginevra","Ginger","Gingras","Ginni","Ginnie","Ginnifer","Ginny","Gino","Ginsberg","Ginsburg","Gintz","Ginzburg","Gio","Giordano","Giorgi","Giorgia","Giorgio","Giovanna","Giovanni","Gipps","Gipson","Gipsy","Giralda","Giraldo","Girand","Girard","Girardi","Girardo","Giraud","Girhiny","Girish","Girovard","Girvin","Gisela","Giselbert","Gisele","Gisella","Giselle","Gish","Gisser","Gitel","Githens","Gitlow","Gitt","Gittel","Gittle","Giuditta","Giule","Giulia","Giuliana","Giulietta","Giulio","Giuseppe","Giustina","Giustino","Giusto","Given","Giverin","Giza","Gizela","Glaab","Glad","Gladdie","Gladdy","Gladi","Gladine","Gladis","Gladstone","Gladwin","Gladys","Glanti","Glantz","Glanville","Glarum","Glaser","Glasgo","Glass","Glassco","Glassman","Glaudia","Glavin","Gleason","Gleda","Gleeson","Gleich","Glen","Glenda","Glenden","Glendon","Glenine","Glenn","Glenna","Glennie","Glennis","Glennon","Glialentn","Glick","Glimp","Glinys","Glogau","Glori","Gloria","Gloriana","Gloriane","Glorianna","Glory","Glover","Glovsky","Gluck","Glyn","Glynas","Glynda","Glynias","Glynis","Glynn","Glynnis","Gmur","Gnni","Goar","Goat","Gobert","God","Goda","Godard","Godart","Godbeare","Godber","Goddard","Goddart","Godden","Godderd","Godding","Goddord","Godewyn","Godfree","Godfrey","Godfry","Godiva","Godliman","Godred","Godric","Godrich","Godspeed","Godwin","Goebel","Goeger","Goer","Goerke","Goeselt","Goetz","Goff","Goggin","Goines","Gokey","Golanka","Gold","Golda","Goldarina","Goldberg","Golden","Goldenberg","Goldfarb","Goldfinch","Goldi","Goldia","Goldie","Goldin","Goldina","Golding","Goldman","Goldner","Goldshell","Goldshlag","Goldsmith","Goldstein","Goldston","Goldsworthy","Goldwin","Goldy","Goles","Golightly","Gollin","Golliner","Golter","Goltz","Golub","Gomar","Gombach","Gombosi","Gomer","Gomez","Gona","Gonagle","Gone","Gonick","Gonnella","Gonroff","Gonsalve","Gonta","Gonyea","Gonzales","Gonzalez","Gonzalo","Goober","Good","Goodard","Goodden","Goode","Goodhen","Goodill","Goodkin","Goodman","Goodrich","Goodrow","Goodson","Goodspeed","Goodwin","Goody","Goodyear","Googins","Gora","Goran","Goraud","Gord","Gordan","Gorden","Gordie","Gordon","Gordy","Gore","Goren","Gorey","Gorga","Gorges","Gorlicki","Gorlin","Gorman","Gorrian","Gorrono","Gorski","Gorton","Gosnell","Gosney","Goss","Gosselin","Gosser","Gotcher","Goth","Gothar","Gothard","Gothart","Gothurd","Goto","Gottfried","Gotthard","Gotthelf","Gottlieb","Gottuard","Gottwald","Gough","Gould","Goulden","Goulder","Goulet","Goulette","Gove","Gow","Gower","Gowon","Gowrie","Graaf","Grace","Graces","Gracia","Gracie","Gracye","Gradeigh","Gradey","Grados","Grady","Grae","Graehl","Graehme","Graeme","Graf","Graff","Graham","Graig","Grail","Gram","Gran","Grand","Grane","Graner","Granese","Grange","Granger","Grani","Grania","Graniah","Graniela","Granlund","Grannia","Granniah","Grannias","Grannie","Granny","Granoff","Grant","Grantham","Granthem","Grantland","Grantley","Granville","Grassi","Grata","Grath","Grati","Gratia","Gratiana","Gratianna","Gratt","Graubert","Gravante","Graves","Gray","Graybill","Grayce","Grayson","Grazia","Greabe","Grearson","Gredel","Greeley","Green","Greenberg","Greenburg","Greene","Greenebaum","Greenes","Greenfield","Greenland","Greenleaf","Greenlee","Greenman","Greenquist","Greenstein","Greenwald","Greenwell","Greenwood","Greer","Greerson","Greeson","Grefe","Grefer","Greff","Greg","Grega","Gregg","Greggory","Greggs","Gregoire","Gregoor","Gregor","Gregorio","Gregorius","Gregory","Gregrory","Gregson","Greiner","Grekin","Grenier","Grenville","Gresham","Greta","Gretal","Gretchen","Grete","Gretel","Grethel","Gretna","Gretta","Grevera","Grew","Grewitz","Grey","Greyso","Greyson","Greysun","Grider","Gridley","Grier","Grieve","Griff","Griffie","Griffin","Griffis","Griffith","Griffiths","Griffy","Griggs","Grigson","Grim","Grimaldi","Grimaud","Grimbal","Grimbald","Grimbly","Grimes","Grimona","Grimonia","Grindlay","Grindle","Grinnell","Gris","Griselda","Griseldis","Grishilda","Grishilde","Grissel","Grissom","Gristede","Griswold","Griz","Grizel","Grizelda","Groark","Grobe","Grochow","Grodin","Grof","Grogan","Groh","Gromme","Grondin","Gronseth","Groome","Groos","Groot","Grory","Grosberg","Groscr","Grose","Grosmark","Gross","Grossman","Grosvenor","Grosz","Grote","Grounds","Grous","Grove","Groveman","Grover","Groves","Grubb","Grube","Gruber","Grubman","Gruchot","Grunberg","Grunenwald","Grussing","Gruver","Gschu","Guadalupe","Gualterio","Gualtiero","Guarino","Gudren","Gudrin","Gudrun","Guendolen","Guenevere","Guenna","Guenzi","Guerin","Guerra","Guevara","Guglielma","Guglielmo","Gui","Guibert","Guido","Guidotti","Guilbert","Guild","Guildroy","Guillaume","Guillema","Guillemette","Guillermo","Guimar","Guimond","Guinevere","Guinn","Guinna","Guise","Gujral","Gula","Gulgee","Gulick","Gun","Gunar","Gunas","Gundry","Gunilla","Gunn","Gunnar","Gunner","Gunning","Guntar","Gunter","Gunthar","Gunther","Gunzburg","Gupta","Gurango","Gurevich","Guria","Gurias","Gurl","Gurney","Gurolinick","Gurtner","Gus","Gusba","Gusella","Guss","Gussi","Gussie","Gussman","Gussy","Gusta","Gustaf","Gustafson","Gustafsson","Gustav","Gustave","Gustavo","Gustavus","Gusti","Gustie","Gustin","Gusty","Gut","Guthrey","Guthrie","Guthry","Gutow","Guttery","Guy","Guyer","Guyon","Guzel","Gwen","Gwendolen","Gwendolin","Gwendolyn","Gweneth","Gwenette","Gwenn","Gwenneth","Gwenni","Gwennie","Gwenny","Gwenora","Gwenore","Gwyn","Gwyneth","Gwynne","Gyasi","Gyatt","Gyimah","Gylys","Gypsie","Gypsy","Gytle","Ha","Haag","Haakon","Haas","Haase","Haberman","Hach","Hachman","Hachmann","Hachmin","Hackathorn","Hacker","Hackett","Hackney","Had","Haddad","Hadden","Haden","Hadik","Hadlee","Hadleigh","Hadley","Hadria","Hadrian","Hadsall","Hadwin","Hadwyn","Haeckel","Haerle","Haerr","Haff","Hafler","Hagai","Hagan","Hagar","Hagen","Hagerman","Haggai","Haggar","Haggerty","Haggi","Hagi","Hagood","Hahn","Hahnert","Hahnke","Haida","Haig","Haile","Hailee","Hailey","Haily","Haim","Haimes","Haines","Hak","Hakan","Hake","Hakeem","Hakim","Hako","Hakon","Hal","Haland","Halbeib","Halbert","Halda","Haldan","Haldane","Haldas","Haldeman","Halden","Haldes","Haldi","Haldis","Hale","Haleigh","Haletky","Haletta","Halette","Haley","Halfdan","Halfon","Halford","Hali","Halie","Halima","Halimeda","Hall","Halla","Hallagan","Hallam","Halland","Halle","Hallee","Hallerson","Hallett","Hallette","Halley","Halli","Halliday","Hallie","Hallock","Hallsy","Hallvard","Hally","Halona","Halonna","Halpern","Halsey","Halstead","Halsted","Halsy","Halvaard","Halverson","Ham","Hama","Hamachi","Hamal","Haman","Hamann","Hambley","Hamburger","Hamel","Hamer","Hamford","Hamforrd","Hamfurd","Hamid","Hamil","Hamilton","Hamish","Hamlani","Hamlen","Hamlet","Hamlin","Hammad","Hammel","Hammer","Hammerskjold","Hammock","Hammond","Hamner","Hamnet","Hamo","Hamon","Hampton","Hamrah","Hamrnand","Han","Hana","Hanae","Hanafee","Hanako","Hanan","Hance","Hancock","Handal","Handbook","Handel","Handler","Hands","Handy","Haney","Hanford","Hanforrd","Hanfurd","Hank","Hankins","Hanleigh","Hanley","Hanna","Hannah","Hannan","Hanni","Hannibal","Hannie","Hannis","Hannon","Hannover","Hannus","Hanny","Hanover","Hans","Hanschen","Hansel","Hanselka","Hansen","Hanser","Hanshaw","Hansiain","Hanson","Hanus","Hanway","Hanzelin","Happ","Happy","Hapte","Hara","Harald","Harbard","Harberd","Harbert","Harbird","Harbison","Harbot","Harbour","Harcourt","Hardan","Harday","Hardden","Hardej","Harden","Hardi","Hardie","Hardigg","Hardin","Harding","Hardman","Hardner","Hardunn","Hardwick","Hardy","Hare","Harelda","Harewood","Harhay","Harilda","Harim","Harl","Harlamert","Harlan","Harland","Harle","Harleigh","Harlen","Harlene","Harley","Harli","Harlie","Harlin","Harlow","Harman","Harmaning","Harmon","Harmonia","Harmonie","Harmony","Harms","Harned","Harneen","Harness","Harod","Harold","Harolda","Haroldson","Haroun","Harp","Harper","Harpole","Harpp","Harragan","Harrell","Harri","Harrie","Harriet","Harriett","Harrietta","Harriette","Harriman","Harrington","Harriot","Harriott","Harris","Harrison","Harrod","Harrow","Harrus","Harry","Harshman","Harsho","Hart","Harte","Hartfield","Hartill","Hartley","Hartman","Hartmann","Hartmunn","Hartnett","Harts","Hartwell","Harty","Hartzel","Hartzell","Hartzke","Harv","Harvard","Harve","Harvey","Harvie","Harvison","Harwell","Harwill","Harwilll","Harwin","Hasan","Hasen","Hasheem","Hashim","Hashimoto","Hashum","Hasin","Haskel","Haskell","Haskins","Haslam","Haslett","Hasseman","Hassett","Hassi","Hassin","Hastie","Hastings","Hasty","Haswell","Hatch","Hatcher","Hatfield","Hathaway","Hathcock","Hatti","Hattie","Hatty","Hau","Hauck","Hauge","Haugen","Hauger","Haughay","Haukom","Hauser","Hausmann","Hausner","Havard","Havelock","Haveman","Haven","Havener","Havens","Havstad","Hawger","Hawk","Hawken","Hawker","Hawkie","Hawkins","Hawley","Hawthorn","Hax","Hay","Haya","Hayashi","Hayden","Haydon","Haye","Hayes","Hayley","Hayman","Haymes","Haymo","Hayne","Haynes","Haynor","Hayott","Hays","Hayse","Hayton","Hayward","Haywood","Hayyim","Hazaki","Hazard","Haze","Hazeghi","Hazel","Hazelton","Hazem","Hazen","Hazlett","Hazlip","Head","Heady","Healey","Healion","Heall","Healy","Heaps","Hearn","Hearsh","Heater","Heath","Heathcote","Heather","Hebbe","Hebe","Hebel","Heber","Hebert","Hebner","Hebrew","Hecht","Heck","Hecker","Hecklau","Hector","Heda","Hedberg","Hedda","Heddi","Heddie","Heddy","Hedelman","Hedgcock","Hedges","Hedi","Hedley","Hedva","Hedvah","Hedve","Hedveh","Hedvig","Hedvige","Hedwig","Hedwiga","Hedy","Heeley","Heer","Heffron","Hefter","Hegarty","Hege","Heger","Hegyera","Hehre","Heid","Heida","Heidi","Heidie","Heidt","Heidy","Heigho","Heigl","Heilman","Heilner","Heim","Heimer","Heimlich","Hein","Heindrick","Heiner","Heiney","Heinrich","Heinrick","Heinrik","Heinrike","Heins","Heintz","Heise","Heisel","Heiskell","Heisser","Hekker","Hekking","Helaina","Helaine","Helali","Helban","Helbon","Helbona","Helbonia","Helbonna","Helbonnah","Helbonnas","Held","Helen","Helena","Helene","Helenka","Helfand","Helfant","Helga","Helge","Helgeson","Hellene","Heller","Helli","Hellman","Helm","Helman","Helmer","Helms","Helmut","Heloise","Helprin","Helsa","Helse","Helsell","Helsie","Helve","Helyn","Heman","Hembree","Hemingway","Hemminger","Hemphill","Hen","Hendel","Henden","Henderson","Hendon","Hendren","Hendrick","Hendricks","Hendrickson","Hendrik","Hendrika","Hendrix","Hendry","Henebry","Heng","Hengel","Henghold","Henig","Henigman","Henka","Henke","Henleigh","Henley","Henn","Hennahane","Hennebery","Hennessey","Hennessy","Henni","Hennie","Henning","Henri","Henricks","Henrie","Henrieta","Henrietta","Henriette","Henriha","Henrik","Henrion","Henrique","Henriques","Henry","Henryetta","Henryk","Henryson","Henson","Hentrich","Hephzibah","Hephzipa","Hephzipah","Heppman","Hepsiba","Hepsibah","Hepza","Hepzi","Hera","Herald","Herb","Herbert","Herbie","Herbst","Herby","Herc","Hercule","Hercules","Herculie","Hereld","Heriberto","Heringer","Herm","Herman","Hermann","Hermes","Hermia","Hermie","Hermina","Hermine","Herminia","Hermione","Hermon","Hermosa","Hermy","Hernandez","Hernando","Hernardo","Herod","Herodias","Herold","Heron","Herr","Herra","Herrah","Herrera","Herrick","Herries","Herring","Herrington","Herriott","Herrle","Herrmann","Herrod","Hersch","Herschel","Hersh","Hershel","Hershell","Herson","Herstein","Herta","Hertberg","Hertha","Hertz","Hertzfeld","Hertzog","Herv","Herve","Hervey","Herwick","Herwig","Herwin","Herzberg","Herzel","Herzen","Herzig","Herzog","Hescock","Heshum","Hesketh","Hesky","Hesler","Hesper","Hess","Hessler","Hessney","Hesta","Hester","Hesther","Hestia","Heti","Hett","Hetti","Hettie","Hetty","Heurlin","Heuser","Hew","Hewart","Hewe","Hewes","Hewet","Hewett","Hewie","Hewitt","Hey","Heyde","Heydon","Heyer","Heyes","Heyman","Heymann","Heyward","Heywood","Hezekiah","Hi","Hibben","Hibbert","Hibbitts","Hibbs","Hickey","Hickie","Hicks","Hidie","Hieronymus","Hiett","Higbee","Higginbotham","Higgins","Higginson","Higgs","High","Highams","Hightower","Higinbotham","Higley","Hijoung","Hike","Hilaire","Hilar","Hilaria","Hilario","Hilarius","Hilary","Hilbert","Hild","Hilda","Hildagard","Hildagarde","Hilde","Hildebrandt","Hildegaard","Hildegard","Hildegarde","Hildick","Hildie","Hildy","Hilel","Hill","Hillard","Hillari","Hillary","Hilleary","Hillegass","Hillel","Hillell","Hiller","Hillery","Hillhouse","Hilliard","Hilliary","Hillie","Hillier","Hillinck","Hillman","Hills","Hilly","Hillyer","Hiltan","Hilten","Hiltner","Hilton","Him","Hime","Himelman","Hinch","Hinckley","Hinda","Hindorff","Hindu","Hines","Hinkel","Hinkle","Hinman","Hinson","Hintze","Hinze","Hippel","Hirai","Hiram","Hirasuna","Hiro","Hiroko","Hiroshi","Hirsch","Hirschfeld","Hirsh","Hirst","Hirz","Hirza","Hisbe","Hitchcock","Hite","Hitoshi","Hitt","Hittel","Hizar","Hjerpe","Hluchy","Ho","Hoag","Hoagland","Hoang","Hoashis","Hoban","Hobard","Hobart","Hobbie","Hobbs","Hobey","Hobie","Hochman","Hock","Hocker","Hodess","Hodge","Hodges","Hodgkinson","Hodgson","Hodosh","Hoebart","Hoeg","Hoehne","Hoem","Hoenack","Hoes","Hoeve","Hoffarth","Hoffer","Hoffert","Hoffman","Hoffmann","Hofmann","Hofstetter","Hogan","Hogarth","Hogen","Hogg","Hogle","Hogue","Hoi","Hoisch","Hokanson","Hola","Holbrook","Holbrooke","Holcman","Holcomb","Holden","Holder","Holds","Hole","Holey","Holladay","Hollah","Holland","Hollander","Holle","Hollenbeck","Holleran","Hollerman","Holli","Hollie","Hollinger","Hollingsworth","Hollington","Hollis","Hollister","Holloway","Holly","Holly-Anne","Hollyanne","Holman","Holmann","Holmen","Holmes","Holms","Holmun","Holna","Holofernes","Holsworth","Holt","Holton","Holtorf","Holtz","Holub","Holzman","Homans","Home","Homer","Homere","Homerus","Homovec","Honan","Honebein","Honey","Honeyman","Honeywell","Hong","Honig","Honna","Honniball","Honor","Honora","Honoria","Honorine","Hoo","Hooge","Hook","Hooke","Hooker","Hoon","Hoopen","Hooper","Hoopes","Hootman","Hoover","Hope","Hopfinger","Hopkins","Hoppe","Hopper","Horace","Horacio","Horan","Horatia","Horatio","Horatius","Horbal","Horgan","Horick","Horlacher","Horn","Horne","Horner","Hornstein","Horodko","Horowitz","Horsey","Horst","Hort","Horten","Hortensa","Hortense","Hortensia","Horter","Horton","Horvitz","Horwath","Horwitz","Hosbein","Hose","Hosea","Hoseia","Hosfmann","Hoshi","Hoskinson","Hospers","Hotchkiss","Hotze","Hough","Houghton","Houlberg","Hound","Hourigan","Hourihan","Housen","Houser","Houston","Housum","Hovey","How","Howard","Howarth","Howe","Howell","Howenstein","Howes","Howey","Howie","Howlan","Howland","Howlend","Howlond","Howlyn","Howund","Howzell","Hoxie","Hoxsie","Hoy","Hoye","Hoyt","Hrutkay","Hsu","Hu","Huai","Huan","Huang","Huba","Hubbard","Hubble","Hube","Huber","Huberman","Hubert","Huberto","Huberty","Hubey","Hubie","Hubing","Hubsher","Huckaby","Huda","Hudgens","Hudis","Hudnut","Hudson","Huebner","Huei","Huesman","Hueston","Huey","Huff","Hufnagel","Huggins","Hugh","Hughes","Hughett","Hughie","Hughmanick","Hugibert","Hugo","Hugon","Hugues","Hui","Hujsak","Hukill","Hulbard","Hulbert","Hulbig","Hulburt","Hulda","Huldah","Hulen","Hull","Hullda","Hultgren","Hultin","Hulton","Hum","Humbert","Humberto","Humble","Hume","Humfrey","Humfrid","Humfried","Hummel","Humo","Hump","Humpage","Humph","Humphrey","Hun","Hunfredo","Hung","Hungarian","Hunger","Hunley","Hunsinger","Hunt","Hunter","Huntingdon","Huntington","Huntlee","Huntley","Huoh","Huppert","Hurd","Hurff","Hurlbut","Hurlee","Hurleigh","Hurless","Hurley","Hurlow","Hurst","Hurty","Hurwit","Hurwitz","Husain","Husch","Husein","Husha","Huskamp","Huskey","Hussar","Hussein","Hussey","Huston","Hut","Hutchings","Hutchins","Hutchinson","Hutchison","Hutner","Hutson","Hutt","Huttan","Hutton","Hux","Huxham","Huxley","Hwang","Hwu","Hy","Hyacinth","Hyacintha","Hyacinthe","Hyacinthia","Hyacinthie","Hyams","Hyatt","Hyde","Hylan","Hyland","Hylton","Hyman","Hymen","Hymie","Hynda","Hynes","Hyo","Hyozo","Hyps","Hyrup","Iago","Iain","Iams","Ian","Iand","Ianteen","Ianthe","Iaria","Iaverne","Ib","Ibbetson","Ibbie","Ibbison","Ibby","Ibrahim","Ibson","Ichabod","Icken","Id","Ida","Idalia","Idalina","Idaline","Idalla","Idden","Iddo","Ide","Idel","Idelia","Idell","Idelle","Idelson","Iden","Idette","Idleman","Idola","Idolah","Idolla","Idona","Idonah","Idonna","Idou","Idoux","Idzik","Iene","Ier","Ierna","Ieso","Ietta","Iey","Ifill","Igal","Igenia","Iggie","Iggy","Iglesias","Ignace","Ignacia","Ignacio","Ignacius","Ignatia","Ignatius","Ignatz","Ignatzia","Ignaz","Ignazio","Igor","Ihab","Iiette","Iila","Iinde","Iinden","Iives","Ike","Ikeda","Ikey","Ikkela","Ilaire","Ilan","Ilana","Ilario","Ilarrold","Ilbert","Ileana","Ileane","Ilene","Iline","Ilise","Ilka","Ilke","Illa","Illene","Illona","Illyes","Ilona","Ilonka","Ilowell","Ilsa","Ilse","Ilwain","Ilysa","Ilyse","Ilyssa","Im","Ima","Imalda","Iman","Imelda","Imelida","Imena","Immanuel","Imogen","Imogene","Imojean","Imray","Imre","Imtiaz","Ina","Incrocci","Indihar","Indira","Inerney","Ines","Inesita","Ineslta","Inessa","Inez","Infeld","Infield","Ing","Inga","Ingaberg","Ingaborg","Ingalls","Ingamar","Ingar","Inge","Ingeberg","Ingeborg","Ingelbert","Ingemar","Inger","Ingham","Inglebert","Ingles","Inglis","Ingmar","Ingold","Ingra","Ingraham","Ingram","Ingrid","Ingrim","Ingunna","Ingvar","Inigo","Inkster","Inman"};

    private static String[] letters = new String[] {"A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","X","Y","Z"};
    
    private static String custLogoUrl = "https://cdn.hravatar.com/web/orgimage/zrWvh1uNWrg-/img_5x1715795136841.png";
    

    static String SAMPLE_ESSAY_CONTENT = "g.SampleEssayContentKey"; // "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Maecenas venenatis lobortis mi ut tincidunt. Nulla in sem eget metus aliquet feugiat vel eget odio. Fusce varius leo lectus, et ullamcorper est tempor et. Cras semper eleifend lacus in rhoncus. Integer ac mauris euismod, hendrerit nisi vitae, porttitor tortor. Integer ut leo sit amet nisl finibus auctor at quis massa. Nullam at erat in sem placerat consectetur nec a diam. Donec non lectus euismod, pulvinar elit nec, dapibus nulla. Phasellus a cursus quam, in pharetra nisi. Fusce porta rutrum turpis a varius. Proin dignissim vitae diam ac fermentum. Morbi neque quam, interdum lobortis neque ac, porttitor hendrerit neque. Vestibulum ut erat consequat, luctus nunc non, maximus justo. Phasellus vel lorem quam. Ut at accumsan arcu. Aliquam erat volutpat. Aliquam quis urna eget est bibendum interdum ultrices vitae diam. Praesent a augue eget elit posuere fermentum ut ut lorem. Morbi magna est, dignissim sit amet risus sed, efficitur ultrices nisl. Pellentesque dignissim enim quis sem rutrum, et condimentum libero mattis. Aliquam venenatis, risus nec hendrerit rhoncus, neque nisi euismod dolor, non dignissim justo lacus vel felis. Curabitur mauris quam, euismod vehicula convallis id, dictum a mauris. Praesent vehicula lectus libero. Morbi in feugiat massa. Donec et dapibus quam, sed feugiat nibh. Integer quam magna, pellentesque vulputate urna quis, ullamcorper scelerisque mi.";

    static String SAMPLE_ESSAY_TITLE = "g.SampleEssayTitleKey"; //  "This is the essay question.";
    
    static String SAMPLE_AUDIO_CONTENT = "g.SampleAudioContentKey";
    
    static String SAMPLE_AUDIO_TITLE = "g.SampleAudioTitleKey";

    static String[] BIODATA_PERF_CAVEATS = new String[] {"Below average productivity history", "Below average performance reviews"};
    static String[] BIODATA_TENURE_CAVEATS = new String[] {"Frequent job changes", "Potential long commute"};
    static String[] BIODATA_UNPROD_CAVEATS = new String[] {"History of frequent extra time off", "May not follow rules if doesn't agree with them"};

    static String[] BIODATA_PERF_INTERVIEW = new String[] {"How does your work compare with your peers? Do you produce more or less? How do you know?", "What kind of feedback have you received about your performance from your managers and your peers?"};
    static String[] BIODATA_TENURE_INTERVIEW = new String[] {"Review your last few jobs with me, explaining why you left the old job and what attracted you to the new one.", "What is the longest distance you have had to commute to work? What did you do during the commute? How long did you keep that job?"};
    static String[] BIODATA_UNPROD_INTERVIEW = new String[] {"From time to time we all need to take time off to deal with personal issues. Can you tell me about the kind of things you've needed time off to take care of during the past 12 months? ", "Not all rules make sense at all times. Tell me about a time when you were faced with a rule you didn't agree with. What did you do?"};


    static synchronized void init()
    {
        if( random!=null )
            return;
        random = new Random();
    }
    
    

    public static String getScoreText( SimJ.Simcompetency simCompetencyObj, float scaledScore, ScoreColorSchemeType scst )
    {
        if( simCompetencyObj == null )
            return null;

        String t = "";


        if( simCompetencyObj.getHighcliffmin()> 0 && simCompetencyObj.getHighclifflevel()>0 && scaledScore >= simCompetencyObj.getHighcliffmin() )
            t += UrlEncodingUtils.decodeKeepPlus( simCompetencyObj.getHighclifftext() == null ? "" : simCompetencyObj.getHighclifftext() );

        else if( scst.getIsSevenColor() && scaledScore >= simCompetencyObj.getWhitemin() )
            t += UrlEncodingUtils.decodeKeepPlus( simCompetencyObj.getWhitetext() == null ? "" : simCompetencyObj.getWhitetext() );

        else if( scaledScore >= simCompetencyObj.getGreenmin() )
            t += UrlEncodingUtils.decodeKeepPlus( simCompetencyObj.getGreentext() == null ? "" : simCompetencyObj.getGreentext() );

        else if( scst.getIsFiveOrSevenColor() && scaledScore >= simCompetencyObj.getYellowgreenmin() )
            t +=  UrlEncodingUtils.decodeKeepPlus( simCompetencyObj.getYellowgreentext() == null ? "" : simCompetencyObj.getYellowgreentext() );

        else if( scaledScore >= simCompetencyObj.getYellowmin() )
            t +=  UrlEncodingUtils.decodeKeepPlus( simCompetencyObj.getYellowtext()==null ? "" : simCompetencyObj.getYellowtext() );

        else if( scst.getIsFiveOrSevenColor() && scaledScore >= simCompetencyObj.getRedyellowmin() )
            t +=  UrlEncodingUtils.decodeKeepPlus( simCompetencyObj.getRedyellowtext() == null ? "" : simCompetencyObj.getRedyellowtext() );

        else if( scst.getIsSevenColor() && scaledScore >= simCompetencyObj.getRedmin() )
            t += UrlEncodingUtils.decodeKeepPlus( simCompetencyObj.getRedtext() == null ? "" : simCompetencyObj.getRedtext() );

        else if( scst.getIsSevenColor()  )
            t += UrlEncodingUtils.decodeKeepPlus( simCompetencyObj.getBlacktext() == null ? "" : simCompetencyObj.getBlacktext() );

        else
            t +=  UrlEncodingUtils.decodeKeepPlus( simCompetencyObj.getRedtext() == null ? "" : simCompetencyObj.getRedtext() );

       return t;
    }



    public static List<String> getStandardCaveatList( Locale locale, String competencyName, int simCompetencyClassId )
    {
        List<String> out = new ArrayList<>();

        SimCompetencyClass scc = SimCompetencyClass.getValue(simCompetencyClassId);

        if( scc.equals( SimCompetencyClass.SCOREDTYPING ) )
        {
            out.add( MessageFactory.getStringMessage( locale , "g.WordPerMinX" , new String[]{ Integer.toString( Math.round( 60 ) )} ) );
            out.add( MessageFactory.getStringMessage( locale , "g.WordPerMinAccAdjX" , new String[]{ Integer.toString( Math.round( 50 ) )} ) );
            out.add( MessageFactory.getStringMessage( locale , "g.AccuracyX" , new String[]{ Integer.toString( Math.round( 85 ) )} ) );
        }

        else if( scc.equals( SimCompetencyClass.SCOREDDATAENTRY ) )
        {
            out.add( MessageFactory.getStringMessage( locale , "g.KeystrokesPerHourX" , new String[]{ Integer.toString( Math.round( 8200 ) )} ) );
            out.add( MessageFactory.getStringMessage( locale , "g.GrossErrorsX" , new String[]{ Integer.toString( Math.round( 1 ) )} ) );
            out.add( MessageFactory.getStringMessage( locale , "g.KeystrokesPerHourAccAdjX" , new String[]{ Integer.toString( Math.round( 7790 ) )} ) );
            out.add( MessageFactory.getStringMessage( locale , "g.AccuracyX" , new String[]{ Integer.toString( Math.round( 95 ) )} ) );
        }

        else if( scc.equals( SimCompetencyClass.SCOREDESSAY ) )
        {
            out.add( MessageFactory.getStringMessage( locale , "g.EssayMachineScoreX" , new String[]{ Integer.toString( Math.round( 80 ) )} ) );
            out.add( MessageFactory.getStringMessage( locale , "g.EssayMachineConfidenceX" , new String[]{ Integer.toString( Math.round( 75 ) )} ) );
            out.add( MessageFactory.getStringMessage( locale , "g.EssayWordCountX" , new String[]{ Integer.toString( Math.round( 247 ) )} ) );                
        }

        else if( scc.equals( SimCompetencyClass.SCOREDAUDIO ) )
        {
            // TODO  TO DO!!!
        }

        else if( scc.equals( SimCompetencyClass.SCOREDAVUPLOAD ) )
        {
            // TODO  TO DO!!!
        }

        else if( scc.equals( SimCompetencyClass.SCOREDBIODATA ) )
        {
            String[] vs = null;


            if( competencyName.contains( "Performance" ) )
                vs = BIODATA_PERF_CAVEATS ;

            if( competencyName.contains( "Tenure" ) )
                vs = BIODATA_TENURE_CAVEATS;

            if( competencyName.contains( "Unproductive" ) )
                vs = BIODATA_UNPROD_CAVEATS;

            if( vs != null )
            {
                for( String v : vs )
                {
                    out.add( v );
                }
            }
        }

        return out;
    }


    public static int getScoreCategoryTypeId( SimJ.Simcompetency sjc, float score, ScoreColorSchemeType scst )
    {
        ScoreCategoryType scoreCat = ScoreCategoryType.getForScore(scst,
                                                                    score,
                                                                    sjc.getHighcliffmin(), 
                                                                    sjc.getWhitemin(),
                                                                    sjc.getGreenmin(),
                                                                    sjc.getYellowgreenmin(),
                                                                    sjc.getYellowmin(),
                                                                    sjc.getRedyellowmin(), 
                                                                    sjc.getRedmin(),
                                                                    0,
                                                                    sjc.getCategorydisttype(),
                                                                    sjc.getHighclifflevel() );

        if( sjc.getCategoryadjustmentthreshold()>0 && score <= sjc.getCategoryadjustmentthreshold() )
            scoreCat = scoreCat.adjustOneLevelUp( scst );

        return scoreCat.getScoreCategoryTypeId();
    }




    public static String packGeneralNoncompetencyResponses( Locale locale, boolean hasWriting, boolean hasAudio, boolean hasAimsCorpCit, boolean hasAimsIntegrity, boolean includeRiskFactors)
    {
        StringBuilder sb = new StringBuilder();

        List<TextAndTitle> ttl;

        if( hasWriting )
        {
            ttl = new ArrayList<>();

            ttl.add( new TextAndTitle( MessageFactory.getStringMessage(locale, SAMPLE_ESSAY_CONTENT), MessageFactory.getStringMessage(locale, SAMPLE_ESSAY_TITLE)  ) );

            sb.append( packResponses( ttl, NonCompetencyItemType.WRITING_SAMPLE.getTitle() ) );
        }

        if( hasAudio )
        {
            ttl = new ArrayList<>();

            ttl.add(new TextAndTitle( MessageFactory.getStringMessage(locale, SAMPLE_AUDIO_CONTENT), MessageFactory.getStringMessage(locale, SAMPLE_AUDIO_TITLE), RuntimeConstants.getLongValue( "sampleAudioIvrItemResponseId" ), 0  ) );

            sb.append( packResponses( ttl, NonCompetencyItemType.SPEAKING_SAMPLE.getTitle() ) );
        }

        if( includeRiskFactors )
        {
            //Add Sample risk factor
            ttl = new ArrayList<>();

            if( hasAimsCorpCit )
                ttl.add( new TextAndTitle( MessageFactory.getStringMessage( locale, "g.CT3Risk_LowCitizenship" ),"" ));
            if( hasAimsIntegrity )
                ttl.add( new TextAndTitle( MessageFactory.getStringMessage( locale, "g.CT3Risk_LowIntegrity" ),"" ));

            //ttl.add( new TextAndTitle( MessageFactory.getStringMessage( locale, "g.CT3Risk_AIMSFaking" ),"" ));
            //ttl.add( new TextAndTitle( MessageFactory.getStringMessage( locale, "g.CT3Risk_AIMSFaking" ),"" ));
            //ttl.add( new TextAndTitle( MessageFactory.getStringMessage( locale, "g.CT3Risk_AIMSFaking" ),"" ));


            sb.append( packResponses( ttl, CT3Constants.CT3RISKFACTORS ) );
        }
        
        return sb.toString();
    }




    public static String packInterviewQuestions( SimJ.Simcompetency sjc )
    {
        SimCompetencyClass scc = SimCompetencyClass.getValue(sjc.getClassid());

        List<InterviewQuestion> iql = new ArrayList<>();

        InterviewQuestion iqq;

        if( scc.equals( SimCompetencyClass.SCOREDBIODATA ) )
        {
            String[] vs = null;

            if( sjc.getName().contains( "Performance" ) )
                vs = BIODATA_PERF_INTERVIEW ;

            if( sjc.getName().contains( "Tenure" ) )
                vs = BIODATA_TENURE_INTERVIEW;

            if( sjc.getName().contains( "Unproductive" ) )
                vs = BIODATA_UNPROD_INTERVIEW;

            if( vs != null )
            {
                for( String v : vs )
                {
                    iqq = new InterviewQuestion( v, "", "", "" );

                    iql.add( iqq );
                }
            }
        }

        else
        {
            for( InterviewQuestionObj iqo : sjc.getInterviewquestion() )
            {
                iqq = new InterviewQuestion( sjc );

                iqq.load( iqo );

                iql.add( iqq );

                if( iql.size() > 1 )
                    break;
            }
        }


        // LogService.logIt( "ScoreManagerBean.packInterviewQuestions() list size=" + iql.size() );

        StringBuilder sb = new StringBuilder();

        for( InterviewQuestion iq : iql )
        {
            if( sb.length()>0 )
                sb.append( Constants.DELIMITER );

            sb.append( iq.getQuestion() + Constants.DELIMITER +  iq.getAnchorHi() + Constants.DELIMITER + iq.getAnchorMed() + Constants.DELIMITER + iq.getAnchorLow() + Constants.DELIMITER + iq.getScoreBreadth() );
            // sb.append( XMLUtils.encodeURIComponent( iq.getQuestion() ) + Constants.DELIMITER + XMLUtils.encodeURIComponent( iq.getAnchorHi() ) + Constants.DELIMITER + XMLUtils.encodeURIComponent( iq.getAnchorMed() ) + Constants.DELIMITER + XMLUtils.encodeURIComponent( iq.getAnchorLow() )  );

        }

        return sb.toString();
    }




    protected static String packResponses( List<TextAndTitle> ttl, String title )
    {
        // LogService.logIt( "ScoreManagerBean.packResponses() AAA " + irl.size() + " title=" + title );

        if( ttl == null || ttl.isEmpty() )
            return "";

        StringBuilder sb = new StringBuilder();

        String tmp;


        tmp = packTextBasedResponses( ttl );

        if( tmp != null && !tmp.isEmpty() )
        {
            if( sb.length() > 0 )
                sb.append( Constants.DELIMITER );

            sb.append( tmp );
        }

        // LogService.logIt( "BaseTestEventScorer.packResponses() " + ";;;" + title+ ";;;" + Constants.DELIMITER + sb.toString() );

        if( sb.length() > 0 )
            return ";;;" + title+ ";;;" + Constants.DELIMITER + sb.toString();

        return "";
    }


    protected static String packTextBasedResponses( List<TextAndTitle> ttl )
    {
        StringBuilder sb = new StringBuilder();

        for( TextAndTitle tt : ttl )
        {
            if( sb.length()>0 )
                sb.append( Constants.DELIMITER );

            sb.append( tt.getTitle() + Constants.DELIMITER + tt.getText() + Constants.DELIMITER + tt.getFlags() + Constants.DELIMITER + (tt.getString1()==null || tt.getString1().isBlank() ? "" : tt.getString1() ) + "~" + (tt.getString2()==null || tt.getString2().isBlank() ? "" : tt.getString2() ) + "~" + (tt.getString3()==null || tt.getString3().isBlank() ? "" : tt.getString3()) + "~" + (tt.getString4()!=null && !tt.getString4().isEmpty() ? tt.getString4() : "") );
            // sb.append( XMLUtils.encodeURIComponent( tt.getTitle() ) + Constants.DELIMITER + XMLUtils.encodeURIComponent( tt.getText() ) + Constants.DELIMITER + XMLUtils.encodeURIComponent( tt.getFlags() ) );
        }

        return sb.toString();
    }

    public static Image getCustLogoImage() {
        
        try
        {
            return ITextUtils.getITextImage(getLocalImageUrl( custLogoUrl ) );
        }
        catch( Exception e )
        {
            LogService.logIt( e, "SampleReportUtils.getCustLogoImage() ");
            return null;
        }
    }
    
    
    private static URL getLocalImageUrl( String fn )
    {

       if( fn==null || fn.isBlank() )
           return null;
       
       try
       {
           if( fn.toLowerCase().startsWith("http") )
               return (new URI(fn)).toURL();

           return (new URI(getBaseImageUrl() + "/" + fn)).toURL();
       }

       catch( MalformedURLException | URISyntaxException e )
       {
           LogService.logIt(e, "SampleReportUtils.getImageUrl() " );
           return null;
       }
    }
    
    private static String getBaseImageUrl()
    {
        return RuntimeConstants.getStringValue( "baseurl" ) + "/resources/images/";
    }


    public static String getRandomEmail()
    {
        if( random==null )
            init();
        
        StringBuilder sb = new StringBuilder();
        for( int i=0;i<8;i++ )
        {
            sb.append( letters[random.nextInt(letters.length)] );
        }
        sb.append("@");
        for( int i=0;i<5;i++ )
        {
            sb.append( letters[random.nextInt(letters.length)] );
        }        
        sb.append( ".com");
        return sb.toString();
    }
    
    
    
    public static String getRandomFirst()
    {
        if( random==null )
            init();
        
        return firstNames[random.nextInt(firstNames.length)];
    }

    public static String getRandomLast()
    {
        if( random==null )
            init();
        
        return lastNames[random.nextInt(lastNames.length)];
    }

    public static String getRandomSuborg()
    {
        if( random==null )
            init();
        
        return "Division " +  letters[random.nextInt(letters.length)];
    }

    public static String getCompanyName()
    {
        return companyName;
    }


}
