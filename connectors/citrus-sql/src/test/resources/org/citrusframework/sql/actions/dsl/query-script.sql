select NAME
from ACTORS
where EPISODE_ID='${episodeId}';

select COUNT(1) as CNT_EPISODES
from EPISODES;
