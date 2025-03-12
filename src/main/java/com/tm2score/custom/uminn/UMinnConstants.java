/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.uminn;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author miker_000
 */
public class UMinnConstants {
    
    
    
    /**
     * Key is simletId
     * 
     * Object[0] = String scenarioText
     *            
     */
    public static Map<Long,Object[]> simletInfoMap;
    
    public static synchronized void init()
    {
        if( simletInfoMap == null )
        {
            simletInfoMap = new HashMap<>();
            
            // This first one if a dummy one for testing on home system.
            simletInfoMap.put( (long) 133, new Object[]{ (long) 407, "Mariam: Do you remember the 28-year old male who came in last week with hip dysplasia? He is getting surgery today and he will be in the hospital for a while. I was on-call that night so I checked him in and we chatted while I took his history. The next day he asked for my phone number. I told him I could not give it to him, but he just friended me on Facebook. I am still on his case and do not want to be rude by rejecting his request when I will be seeing him for the next few weeks. What should I do?" } );

            // These are live.
            Object[] o = new Object[]{ (long) 407, "Mariam: Do you remember the 28-year old male who came in last week with hip dysplasia? He is getting surgery today and he will be in the hospital for a while. I was on-call that night so I checked him in and we chatted while I took his history. The next day he asked for my phone number. I told him I could not give it to him, but he just friended me on Facebook. I am still on his case and do not want to be rude by rejecting his request when I will be seeing him for the next few weeks. What should I do?" };
            simletInfoMap.put( (long) 1554, o );
            simletInfoMap.put( (long) 4409, o );
            
            o =  new Object[]{ (long) 201, "Suzuki: I just met with our program director. Apparently some faculty members think I’m underperforming. One commented that I’m at the level of a fourth year medical student. Another commented that I hand off too many patient care decisions to the attending and faculty. I don’t agree with these comments. I hand off important patient care decisions to the attending because it is her job to supervise us and make sure we don’t make mistakes. What do you think I should do?" };
            simletInfoMap.put( (long) 1552, o );
            simletInfoMap.put( (long) 4410, o );

            o =  new Object[]{ (long) 2, "Jamie: I met with the patient and his wife to get consent for his cholecystectomy tomorrow. Prior to our meeting, he and his wife told the nurse that they would not need interpreter services because their adult daughter helps translate for them. They plan to speak with her later tonight and review our conversation. During the meeting, they did not talk very much. However, they nodded along during the discussion and when I asked questions. He then signed all of the consent forms for his procedure. How should I handle this?" };
            simletInfoMap.put( (long) 1555, o );
            simletInfoMap.put( (long) 4411, o );
                        
            o =  new Object[]{ (long) 303, "Damon: Hey, you know Julia Fields, that news anchor on TV? I just helped with the delivery of her baby girl! She’s going to name her Jessica." };
            simletInfoMap.put( (long) 1557, o );
            simletInfoMap.put( (long) 4412, o );

            o =  new Object[]{ (long) 501, "Shannon: I told you two hours ago your patient in 217 is getting worse and should be transferred to the ICU immediately. \n" +
                                                                            "\n" +
                                                                            "Suzuki: Yes, I know you told me that but I disagree. She is stable and does not need to be transferred.\n" +
                                                                            "\n" +
                                                                            "Shannon: I think if you examined the patient again you would feel differently.\n" +
                                                                            "\n" +
                                                                            "Suzuki: I did examine her again and I still don’t think she needs to be transferred (nurse leaves). \n" +
                                                                            "\n" +
                                                                            "Suzuki (turning to you): I give up. What do you think I should do?" };
            simletInfoMap.put( (long) 1561, o );
            simletInfoMap.put( (long) 4413, o );

            o =  new Object[]{ (long) 504, "Mariam: I was just rounding on a patient with Dr. Jones, who asked me if I knew how to change a patient’s dressing. I told him of course I do. On the way out the door he said “How do you people maintain proper hygiene for patients when you wear those headscarves?” What should I do about this?" };
            simletInfoMap.put( (long) 1562, o );
            simletInfoMap.put( (long) 4414, o );

            o =  new Object[]{ (long) 505, "Jamie: Yesterday an ID consultant recommended stopping medication for one of our patients. Our primary care team disagreed. We thought this patient should be kept on the same medication but at a reduced dose. We spoke to the fellow on the ID service and she agreed the best path was to reduce the medication. We reduced the medication but did not communicate our decision to the initial ID consultant. Today the consultant returned and was furious that the medication was not stopped. He said “if you’re aren’t going to follow our recommendation, then don’t ask for our recommendation.” How should I handle this?" };
            simletInfoMap.put( (long) 1566, o );
            simletInfoMap.put( (long) 4415, o );

            o =  new Object[]{ (long) 202, "Jackie: In conclusion, the patient’s care is complicated because I’ve never seen this condition before. Dr. Smith, do you have any advice?\n" +
                                                                            "\n" +
                                                                            "Dr. Smith: In this institution, I have noticed that we almost always treat this condition with the medications the patient is already on. I have a hypothesis as to why, but I don’t know the full evidence behind it. What did you find in the literature?\n" +
                                                                            "\n" +
                                                                            "Jackie: When I looked, I did not see anything on UpToDate that addressed the treatment of patients with this condition. I guess we’ll just continue the current treatment.\n" +
                                                                            "\n" +
                                                                            "Jackie (turning to you): Do you have any other ideas?" };
            simletInfoMap.put( (long) 1567, o );
            simletInfoMap.put( (long) 4416, o );

            o =  new Object[]{ (long) 304, "Emily: I was just with a patient who needed a central line inserted in her internal jugular vein. During my anesthesiology rotation, I learned that the new standard of care requires providers to use an ultrasound while placing the line. However, the surgery attending, who was aware of this new guidance, instructed me not to use an ultrasound. He said the old-fashioned way is just as good. How should I handle this situation in the future?" };
            simletInfoMap.put( (long) 1568, o );
            simletInfoMap.put( (long) 4417, o );

            o =  new Object[]{ (long) 605, "Danielle: I just tried to conduct a physical examination on an older white male who came to the ER complaining of abdominal pain. He told me there was no way he was going to be examined by a female resident. He said he wanted to see a male doctor. What should I do?" };
            simletInfoMap.put( (long) 1570, o );
            simletInfoMap.put( (long) 4418, o );

            o =  new Object[]{ (long) 301, "Lola: Lately Peter has been late for many of his shifts. I have been covering for him but he is showing up later and later.  Not only is he late, but his work seems to be slipping too. I don't know why he is acting this way. I know other residents have noticed these issues too. However, I don’t think any of them will speak to the chief resident about the issue. How should I handle this?" };
            simletInfoMap.put( (long) 1571, o );
            simletInfoMap.put( (long) 4419, o );

            o =  new Object[]{ (long) 702, "Jackie: One of my patients has been struggling with pain control. On three occasions I’ve gone to see him and made recommendations to the nurse. Up until now the night shift nurses felt the situation was under control. Now a new nurse has come on duty and is telling me the patient is in pain even with the current pain medication and wants me to evaluate him right away. I have many things to do before my shift is over. What do you think I should do?" };
            simletInfoMap.put( (long) 1576, o );
            simletInfoMap.put( (long) 4420, o );

            o =  new Object[]{ (long) 405, "Jamie: Earlier today I consented an elderly patient for an operation. In an effort to be truthful, I described the procedure in some detail but my attending just told me I upset and frightened the patient by being so explicit. Apparently the patient felt unable to talk to me about her concerns. What should I do?" };
            simletInfoMap.put( (long) 1575, o );
            simletInfoMap.put( (long) 4421, o );

            o =  new Object[]{ (long) 701, "Damon: I’m having a really busy night. I have three new patients waiting to be seen and a patient who has suddenly developed shortness of breath. There are notes to be written and I haven’t had anything to drink for six hours. Finally, the ward nurse just called to tell me the patient I admitted a few hours ago with back pain has increasing pain and is asking for more medication. What do you think I should I tell the nurse?" };
            simletInfoMap.put( (long) 1574, o );
            simletInfoMap.put( (long) 4422, o );

            o =  new Object[]{ (long) 102, "Danielle: It’s been an extremely busy night and I can barely keep my eyes open. I have four hours to go until the end of my shift but I’m worried about my ability to think straight and make good decisions. What should I do?" };
            simletInfoMap.put( (long) 1573, o );
            simletInfoMap.put( (long) 4423, o );
        }
        
    }
    
    public static Object[] getInfoForSimletId( long simletId )
    {
        init();
        
        return simletInfoMap.get( (Long) simletId );
    }
    
}
