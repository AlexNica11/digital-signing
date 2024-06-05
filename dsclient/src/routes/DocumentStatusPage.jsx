import {useEffect, useState} from "react";
import axios from "axios";
import secureLocalStorage from "react-secure-storage";

export default function DocumentStatusPage(){
    const [runningJobs, setRunningJobs] = useState([]);
    const [completedJobs, setCompletedJobs] = useState([]);

    useEffect(() => {
        const getData = () => {
            axios({
                method: 'post',
                url: `/api/service/runningSigningJobs`,
                headers: {
                    Authorization: "Bearer " + secureLocalStorage.getItem("securityToken"),
                    Accept: 'application/json',
                }
            }).then((res) => {
                console.log(res.data);
                setRunningJobs(res.data);
            }).catch((error) => {
                console.error(error);
            });

            axios({
                method: 'post',
                url: `/api/service/completedSigningJobs`,
                headers: {
                    Authorization: "Bearer " + secureLocalStorage.getItem("securityToken"),
                    Accept: 'application/json',
                }
            }).then((res) => {
                console.log(res.data);
                setCompletedJobs(res.data);
            }).catch((error) => {
                console.error(error);
            });
        }
        getData();

        // Polling interval
        const interval = setInterval(getData, 1000);

        // Cleanup on component unmount
        return () => clearInterval(interval);

    }, []);


    const getFile = (e) => {
        const jobId = e.target.textContent;

        console.log(jobId)

        axios({
            method: 'post',
            url: `/api/service/getDocument`,
            data: jobId,
            responseType: 'blob', // important
            headers: {
                Authorization: "Bearer " + secureLocalStorage.getItem("securityToken"),
                'Content-Type': 'text/plain',
                Accept: "*/*",
            }
        }).then((res) => {
            let extension = "";
            switch (res.headers['content-type']) {
                case "application/pdf":
                    extension = ".pdf";
                    break;
                case "application/xml":
                case "text/xml":
                case "application/atom+xml":
                    extension = ".xml";
                    break;
                case "application/json":
                    extension = ".json";
                    break;
                default:
                    extension = ".p7m";
            }
            console.log(extension);
            console.log(res.data.length);
            // console.log(res.data);

            // create link browser memory
            const href = URL.createObjectURL(res.data);

            // create a link element, set the download attribute, and click it
            const link = document.createElement('a');
            link.href = href;
            link.setAttribute('download', jobId + extension);
            document.body.appendChild(link);
            link.click();

            // cleanup link element & remove ObjectUrl
            document.body.removeChild(link);
            URL.revokeObjectURL(href);

        }).catch((error) => {
            console.error(error);
        });

    }

    return (
        <>
            <h1>DocumentStatusPage</h1>
            <div>
                <h2>Running Jobs</h2>
                <ul>
                    {runningJobs.map((rj) =>
                        <li key={rj.jobId}>
                            <label>{rj.jobId}</label>
                        </li>
                    )}
                </ul>
            </div>
            <div>
                <h2>Completed Jobs</h2>
                <h4>Press on job name to download</h4>
                <ul>
                    {completedJobs.map((cj) =>
                        <li key={cj.jobId}>
                            <button onClick={getFile}>{cj.jobId}</button>
                        </li>
                    )}
                </ul>
            </div>
        </>
    );
}
