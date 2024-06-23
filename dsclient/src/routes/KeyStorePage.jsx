import {NavLink, useNavigate, useParams} from "react-router-dom";
import {useEffect, useState} from "react";
import axios from "axios";
import secureLocalStorage from "react-secure-storage";

export default function KeyStorePage(){
    let {keyStoreName} = useParams();
    const [pkParams, setPkParams] = useState([]);
    const navigate = useNavigate();

    const handleDeleteKeyStore = (e) => {
        if (keyStoreName !== "") {
            axios({
                method: 'post',
                url: `/api/users/deleteKeyStore`,
                data: keyStoreName,
                headers: {
                    Authorization: "Bearer " + secureLocalStorage.getItem("securityToken"),
                    'Content-Type': 'text/plain'
                }
            }).catch((error) => {
                console.error(error);
            });
            navigate("/");
        }
    };

    useEffect(() => {
        axios({
            method: 'post',
            url: `/api/users/privateKeyParams`,
            data: keyStoreName,
            headers: {
                Authorization: "Bearer " + secureLocalStorage.getItem("securityToken"),
                Accept: 'application/json',
                'Content-Type': 'text/plain'
            }
        }).then((res) => {
            setPkParams(res.data);
        }).catch((error) => {
            console.error(error);
        });
    }, [])



    return (
        <>
            <h3>Key Store</h3>
            <blockquote><h4>{keyStoreName}</h4></blockquote>
            <button onClick={handleDeleteKeyStore} className="btn outlined icon-right light-blue-text accent-4">
                Delete Key Store
                <i className="material-icons">remove</i>
            </button>
            <div>
                <h5>Key Store Entries</h5>
                <ul className="collection">
                    {pkParams.map((pk) =>
                        <li className="collection-item light-blue-text accent-4" key={pk}>
                            <h6 className="">{pk}</h6>
                        </li>
                    )}
                </ul>
            </div>
        </>
    );
}
